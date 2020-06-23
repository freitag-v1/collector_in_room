package swcapstone.freitag.springsecurityjpa.service;

import com.google.gson.Gson;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.ClassDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDtoWithClassDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.BoundingBoxEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ClassEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.*;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    RequestService requestService;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ClassRepository classRepository;
    @Autowired
    ProjectRepositoryImpl projectRepositoryImpl;
    @Autowired
    ObjectStorageApiClient objectStorageApiClient;
    @Autowired
    ProblemRepository problemRepository;
    @Autowired
    BoundingBoxRepository boundingBoxRepository;

    private static final int COST_PER_DATA = 50;
    private int projectIdTurn;
    protected int problemIdTurn;

    private int getProjectIdTurn() {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findTopByOrderByProjectIdDesc();

        if (projectEntityWrapper.isEmpty())
            return 1;

        return projectEntityWrapper.get().getProjectId() + 1;

    }

    protected int getProblemIdTurn() {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findTopByOrderByProblemIdDesc();

        if (problemEntityWrapper.isEmpty())
            return 1;

        return problemEntityWrapper.get().getProblemId() + 1;
    }

    @Transactional
    public void createProject(HttpServletRequest request, String userId, String bucketName, HttpServletResponse response)
            throws NullPointerException {

        projectIdTurn = getProjectIdTurn();
        int projectId = this.projectIdTurn;
        String projectName = requestService.getProjectNameP(request);
        // String bucketName;  // 의뢰자가 업로드하는 라벨링 데이터를 담을 버킷 - userId+projectName 조합
        // String status;  // 없음, 진행중, 완료
        String workType = requestService.getWorkTypeP(request);
        String dataType = requestService.getDataTypeP(request);
        String subject = requestService.getSubjectP(request);
        // int difficulty;
        String wayContent = requestService.getWayContentP(request);  // 작업 방법
        String conditionContent = requestService.getConditionContentP(request);    // 작업 조건
        // String exampleContent;
        String description = requestService.getDescriptionP(request); // 프로젝트 설명
        int totalData = requestService.getTotalDataP(request);    // 의뢰자가 원하는 수집 데이터 개수
        // int progressData;
        // int cost;

        ProjectDto projectDto = new ProjectDto(projectId, userId, projectName, bucketName, "없음", workType, dataType, subject,
                0, wayContent, conditionContent, "없음", description, totalData, 0, 0, 0);

        if (projectRepository.save(projectDto.toEntity()) == null) {
            response.setHeader("create", "fail");
            return;
        }

        response.setHeader("create", "success");
        response.setHeader("projectId", String.valueOf(projectId));
        return;
    }

    @Transactional
    public void createClass(HttpServletRequest request, HttpServletResponse response) {

        String[] classNameList = requestService.getClassNameListP(request);

        if (classNameList == null) {
            response.setHeader("class", "fail");
            return;
        }

        int projectId = requestService.getProjectIdP(request);

        for (String className : classNameList) {
            ClassDto classDto = new ClassDto(projectId, className);
            if (classRepository.save(classDto.toEntity()) == null) {
                response.setHeader("class", "fail");
                return;
            }
        }

        String bucketName = getBucketName(projectId);

        response.setHeader("bucketName", bucketName);
        response.setHeader("class", "success");
        return;

    }

    @Transactional
    public void uploadExampleContent(HttpServletRequest request, MultipartFile file, HttpServletResponse response) throws Exception {

        String fileName = file.getOriginalFilename();
        String bucketName = requestService.getBucketNameH(request);

        File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + fileName);
        // MultipartFile.transferTo() : 요청 시점의 임시 파일을 로컬 파일 시스템에 영구적으로 복사하는 역할을 수행
        file.transferTo(destinationFile);

        String exampleContent = objectStorageApiClient.putObject(bucketName, destinationFile);

        int projectId = setExampleContent(bucketName, exampleContent, response);

        if (projectId != -1) {

            // 수집 프로젝트이던 라벨링 프로젝트이던 projetId 알려줌
            response.setHeader("projectId", String.valueOf(projectId));

            // 수집 프로젝트는 예시 데이터 업로드 성공하면 바로 결제할 수 있도록 cost 계산해서 헤더에 쓰기
            if (isCollection(projectId))
                setCost(projectId, response);

                // System.out.println("status: 없음 - 결제만 하면 됨. 그 외 프로젝트 생성 작업은 모두 완료");
            else
                response.setHeader("bucketName", bucketName);
        }
    }

    @Transactional
    protected int setExampleContent(String bucketName, String exampleContent, HttpServletResponse response) {

        if (exampleContent == null)
            return -1;

        // System.out.println("exampleContent: "+exampleContent);
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByBucketName(bucketName);

        // status 없음 아니라면 -1 리턴
        if (!projectEntityWrapper.get().getStatus().equals("없음"))
            return -1;

        projectEntityWrapper.ifPresent(selectProject -> {
            selectProject.setExampleContent(exampleContent);

            projectRepository.save(selectProject);
            response.setHeader("example", "success");
        });

        return projectEntityWrapper.get().getProjectId();
    }

    @Transactional
    protected void setCost(int projectId, HttpServletResponse response) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        // status 없음 아니라면 종료
        if (!projectEntityWrapper.get().getStatus().equals("없음"))
            return;

        int totalData = projectEntityWrapper.get().getTotalData();
        int cost = calculateBasicCost(totalData);

        // cost 계산이 잘못 되었으면 종료
        if (cost == -1) {
            return;
        }

        projectEntityWrapper.ifPresent(selectProject -> {
            selectProject.setCost(cost);

            projectRepository.save(selectProject);
            response.setHeader("cost", String.valueOf(cost));
        });

    }

    public boolean isCollection(int projectId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        if (projectEntityWrapper.isPresent() &
                projectEntityWrapper.get().getWorkType().equals("collection"))
            return true;

        return false;
    }

    // 결제 후 프로젝트 상태 없음 -> 진행중 변경
    @Transactional
    public void setStatus(int projectId, HttpServletResponse response) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        // status 없음 아니라면 종료
        if (!projectEntityWrapper.get().getStatus().equals("없음"))
            return;

        projectEntityWrapper.ifPresent(selectProject -> {
            selectProject.setStatus("진행중");

            projectRepository.save(selectProject);
            response.setHeader("status", "진행중");
        });
    }

    public int calculateBasicCost(int totalData) {
        return COST_PER_DATA * totalData;
    }

    // 결제 미완료된 프로젝트 비용 가져오기
    public int getCost(int projectId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        // status 없음 아니라면 종료
        if (!projectEntityWrapper.get().getStatus().equals("없음"))
            return -1;

        if (projectEntityWrapper.isPresent()) {
            return projectEntityWrapper.get().getCost();
        }

        return -1;
    }

    // 결제 미완료된 프로젝트 버킷 이름 가져오기
    private String getBucketName(int projectId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        // status 없음 아니라면 종료
        if (!projectEntityWrapper.get().getStatus().equals("없음"))
            return null;

        if (projectEntityWrapper.isPresent()) {
            return projectEntityWrapper.get().getBucketName();
        }

        return null;
    }

    // 프로젝트 검색 결과 반환
    // workType, dataType, subject, difficulty
    public List<ProjectDtoWithClassDto> getSearchResults(HttpServletRequest request, HttpServletResponse response) {

        // progressData == totalData라면?

        String workType = requestService.getWorkTypeP(request);
        String dataType = requestService.getDataTypeP(request); // image, audio, text / boundingBox, classfication
        String subject = requestService.getSubjectP(request);

        List<ProjectEntity> projectEntityList = projectRepositoryImpl.projectSearch(workType, dataType, subject);

        if (!projectEntityList.isEmpty()) {
            List<ProjectDto> searchResults = ObjectMapperUtils.mapAll(projectEntityList, ProjectDto.class);

            // 클래스 정보도 함께 주기 위해서 ProjectDto -> ProjectDtoWithClassDto 변환
            List<ProjectDtoWithClassDto> searchResultsWithClassNames = withClassDtos(searchResults);

            response.setHeader("search", "success");
            return searchResultsWithClassNames;
        }

        response.setHeader("search", "fail");
        return null;
    }

    private List<ProjectDtoWithClassDto> withClassDtos(List<ProjectDto> projectDtos) {

        if (projectDtos.isEmpty())
            return null;

        List<ProjectDtoWithClassDto> results = new ArrayList<>();

        for (ProjectDto p : projectDtos) {

            int projectId = p.getProjectId();

            List<ClassEntity> classEntities = classRepository.findAllByProjectId(projectId);
            List<ClassDto> classNameList = ObjectMapperUtils.mapAll(classEntities, ClassDto.class);

            ProjectDtoWithClassDto pc = new ProjectDtoWithClassDto(p, classNameList);
            results.add(pc);
        }

        return results;
    }


    // problem_table에 결제 완료된 project_id에 해당하는 문제가 만들어졌니?
    @Transactional
    public void createProblem(int projectId, HttpServletResponse response) {

        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);
        String bucketName = projectEntityWrapper.get().getBucketName();

        if (projectEntityWrapper.get().getStatus().equals("진행중")) {
            int totalData = projectEntityWrapper.get().getTotalData();

            for (int i = 0; i < totalData; i++) {

                problemIdTurn = getProblemIdTurn();
                int problemId = this.problemIdTurn;

                ProblemDto problemDto = new ProblemDto(problemId, projectId, -1
                        , bucketName, null, null, null, "작업전", null, null);

                if (problemRepository.save(problemDto.toEntity()) == null) {
                    response.setHeader("createProblem" + problemDto.getProblemId(), "fail");
                    break;
                }
            }
        }
    }

    // 본인이 의뢰한 작업 목록 확인
    public List<ProjectDtoWithClassDto> getProjectList(String userId, HttpServletResponse response) {

        List<ProjectEntity> projectEntityList = projectRepository.findAllByUserId(userId);

        if (projectEntityList.isEmpty()) {
            response.setHeader("list", "none");
            return null;
        }

        List<ProjectDto> projectDtoList = ObjectMapperUtils.mapAll(projectEntityList, ProjectDto.class);
        return withClassDtos(projectDtoList);
    }


    // work service only
    @Transactional
    protected void setProgressData(int projectId, int numbOfProb) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        if (projectEntityWrapper.isEmpty())
            return;

        projectEntityWrapper.ifPresent(selectProject -> {
            int progressData = selectProject.getProgressData();

            progressData += numbOfProb;
            selectProject.setProgressData(progressData);

            if (selectProject.getTotalData() == selectProject.getProgressData()) {
                selectProject.setStatus("검증대기");    // 작업후 -> 검증대기
            }

            projectRepository.save(selectProject);
        });
    }

    // work service only
    public int getLimit(int projectId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        int limit = projectEntityWrapper.get().getTotalData() - projectEntityWrapper.get().getProgressData();
        return limit;
    }


    // 검증 완료 문제 주기
    public JSONObject getCrossValidationDetails(HttpServletRequest request, HttpServletResponse response) {
        int projectId = requestService.getProjectIdP(request);

        JSONObject jsonObject = findValidatedData(projectId);
        return jsonObject;
    }

    @Transactional
    protected JSONObject findValidatedData(int projectId) {
        // 의뢰자가 만든 검증완료된 문제들(원본) 가져오기
        List<ProblemEntity> validatedProblems =
                problemRepository.findAllByProjectIdAndReferenceIdAndValidationStatus(projectId, -1, "검증완료");

        JSONArray problemArr = new JSONArray();

        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);
        Boolean isBoundingBox;

        if (projectEntityWrapper.get().getDataType().equals("boundingBox"))
            isBoundingBox = true;
        else
            isBoundingBox = false;

        for (ProblemEntity p : validatedProblems) {

            int problemId = p.getProblemId();
            String objectName = p.getObjectName();
            String finalAnswer = p.getFinalAnswer();

            JSONObject eachProblem = new JSONObject();

            JSONObject problemDetails = new JSONObject();
            problemDetails.put("objectName", objectName);

            if (isBoundingBox) {
                String[] boxIds = finalAnswer.split(" ");

                JSONArray boxArr = new JSONArray();
                for(String s : boxIds) {
                    int boxId = Integer.parseInt(s);

                    Optional<BoundingBoxEntity> boundingBoxEntity = boundingBoxRepository.findByBoxId(boxId);
                    String className = boundingBoxEntity.get().getClassName();
                    String coordinates = boundingBoxEntity.get().getCoordinates();

                    JSONObject boxDetails = new JSONObject();
                    boxDetails.put("className", className);
                    boxDetails.put("coordinates", coordinates);

                    boxArr.put(boxDetails);
                }

                problemDetails.put("boundingBoxList", boxArr);
            } else {
                String className = p.getFinalAnswer();
                problemDetails.put("className", className);
            }

            eachProblem.put(String.valueOf(problemId), problemDetails);
            problemArr.put(eachProblem);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("problems", problemArr);
        return jsonObject;
    }

}
