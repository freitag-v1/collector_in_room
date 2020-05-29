package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.ClassDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDtoWithClassDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ClassEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ClassRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProblemRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepositoryImpl;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

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

    private static final int COST_PER_DATA = 50;
    private int projectIdTurn;
    protected int problemIdTurn;

    public int howManyProjects(String userId) {
        List<ProjectEntity> projectEntityList = projectRepository.findAllByUserId(userId);
        return projectEntityList.size();
    }

    private int getProjectIdTurn() {
        int count = (int) projectRepository.count();
        return ++count;
    }

    protected int getProblemIdTurn() {
        int count = (int) problemRepository.count();
        return ++count;
    }

    @Transactional
    public void createProject(HttpServletRequest request, String userId, String bucketName, HttpServletResponse response)
            throws NullPointerException {

        projectIdTurn = getProjectIdTurn();
        int projectId = this.projectIdTurn;
        String projectName = request.getParameter("projectName");
        // String bucketName;  // 의뢰자가 업로드하는 라벨링 데이터를 담을 버킷 - userId+projectName 조합
        // String status;  // 없음, 진행중, 완료
        String workType = request.getParameter("workType"); // collection / labelling
        String dataType = request.getParameter("dataType"); // image, audio, text / boundingBox, classfication
        String subject = request.getParameter("subject");
        // int difficulty;
        String wayContent = request.getParameter("wayContent");  // 작업 방법
        String conditionContent = request.getParameter("conditionContent");    // 작업 조건
        // String exampleContent;
        String description = request.getParameter("description"); // 프로젝트 설명
        String total = request.getParameter("totalData");   // 라벨링은 -1로 설정
        int totalData = Integer.parseInt(total);    // 의뢰자가 원하는 수집 데이터 개수
        // int progressData;
        // int cost;

        ProjectDto projectDto = new ProjectDto(projectId, userId, projectName, bucketName, "없음", workType, dataType, subject,
                0, wayContent, conditionContent, "없음", description, totalData, 0, 0);

        if(projectRepository.save(projectDto.toEntity()) == null) {
            response.setHeader("create", "fail");
            return;
        }

        response.setHeader("create", "success");
        response.setHeader("projectId", String.valueOf(projectId));
        return;
    }

    @Transactional
    public void createClass(HttpServletRequest request, HttpServletResponse response) {

        // 수정
        String[] classNameList = request.getParameterValues("className");
        String strProjectId = request.getParameter("projectId");

        if(classNameList == null) {
            response.setHeader("class", "fail");
            return;
        }

        int projectId = Integer.parseInt(strProjectId);

        for(String className : classNameList) {
            ClassDto classDto = new ClassDto(projectId, className);
            if(classRepository.save(classDto.toEntity()) == null) {
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
        String bucketName = getBucketName(request);

        File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + "exampleContent" + fileName);
        // MultipartFile.transferTo() : 요청 시점의 임시 파일을 로컬 파일 시스템에 영구적으로 복사하는 역할을 수행
        file.transferTo(destinationFile);

        String exampleContent = objectStorageApiClient.putObject(bucketName, destinationFile);

        int projectId = setExampleContent(bucketName, exampleContent, response);
        if(projectId != -1) {

            // 수집 프로젝트이던 라벨링 프로젝트이던 projetId 알려줌
            response.setHeader("projectId", String.valueOf(projectId));

            // 수집 프로젝트는 예시 데이터 업로드 성공하면 바로 결제할 수 있도록 cost 계산해서 헤더에 쓰기
            if(isCollection(projectId))
                setCost(projectId, response);

            // System.out.println("status: 없음 - 결제만 하면 됨. 그 외 프로젝트 생성 작업은 모두 완료");
            else
                response.setHeader("bucketName", bucketName);
        }
    }

    protected String getBucketName(HttpServletRequest request) {
        String bucketName = request.getHeader("bucketName");
        return bucketName;
    }

    @Transactional
    protected int setExampleContent(String bucketName, String exampleContent, HttpServletResponse response) {

        if(exampleContent == null)
            return -1;

        // System.out.println("exampleContent: "+exampleContent);
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByBucketName(bucketName);

        // status 없음 아니라면 -1 리턴
        if(! projectEntityWrapper.get().getStatus().equals("없음"))
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
        if(! projectEntityWrapper.get().getStatus().equals("없음"))
            return;

        int totalData = projectEntityWrapper.get().getTotalData();
        int cost = calculateBasicCost(totalData);

        // cost 계산이 잘못 되었으면 종료
        if( cost == -1) {
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

        if(projectEntityWrapper.isPresent() &
            projectEntityWrapper.get().getWorkType().equals("collection"))
            return true;

        return false;
    }

    // 결제 후 프로젝트 상태 없음 -> 진행중 변경
    @Transactional
    public void setStatus(int projectId, HttpServletResponse response) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        // status 없음 아니라면 종료
        if(! projectEntityWrapper.get().getStatus().equals("없음"))
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
        if(! projectEntityWrapper.get().getStatus().equals("없음"))
            return -1;

        if(projectEntityWrapper.isPresent()) {
            return projectEntityWrapper.get().getCost();
        }

        return -1;
    }

    // 결제 미완료된 프로젝트 버킷 이름 가져오기
    private String getBucketName(int projectId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        // status 없음 아니라면 종료
        if(! projectEntityWrapper.get().getStatus().equals("없음"))
            return null;

        if(projectEntityWrapper.isPresent()) {
            return projectEntityWrapper.get().getBucketName();
        }

        return null;
    }

    // 프로젝트 검색 결과 반환
    // workType, dataType, subject, difficulty
    public List<ProjectDtoWithClassDto> getSearchResults(HttpServletRequest request, HttpServletResponse response) {

        String workType = request.getParameter("workType"); // collection / labelling
        String dataType = request.getParameter("dataType"); // image, audio, text / boundingBox, classfication
        String subject = request.getParameter("subject");

        String strDifficulty = request.getParameter("difficulty");
        int difficulty = Integer.parseInt(strDifficulty);

        List<ProjectEntity> projectEntityList = projectRepositoryImpl.findDynamicQuery(workType, dataType, subject, difficulty);

        if(!projectEntityList.isEmpty()) {
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

        if(projectDtos.isEmpty())
            return null;

        List<ProjectDtoWithClassDto> results = new ArrayList<>();

        for(ProjectDto p : projectDtos) {

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

            for(int i = 0; i < totalData; i++) {

                problemIdTurn = getProblemIdTurn();
                int problemId = this.problemIdTurn;

                ProblemDto problemDto = new ProblemDto(problemId, projectId, -1, bucketName, null, null, "작업전", null);
                if (problemRepository.save(problemDto.toEntity()) == null) {
                    response.setHeader("createProblem"+problemDto.getProblemId(), "fail");
                    break;
                }
            }
        }
    }


    // work service only
    @Transactional
    public void setProgressData(int projectId, MultipartHttpServletRequest uploadRequest) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        if (!projectEntityWrapper.isPresent())
            return;

        int numberOfData = uploadRequest.getFiles("files").size();

        projectEntityWrapper.ifPresent(selectProject -> {
            int progressData = selectProject.getProgressData();

            progressData += numberOfData;
            selectProject.setProgressData(progressData);

            projectRepository.save(selectProject);
        });
    }


    // work service only
    public int getLimit(int projectId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        int limit = projectEntityWrapper.get().getTotalData() - projectEntityWrapper.get().getProgressData();
        return limit;
    }
}
