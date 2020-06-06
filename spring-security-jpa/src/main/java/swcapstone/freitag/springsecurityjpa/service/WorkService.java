package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.*;
import swcapstone.freitag.springsecurityjpa.domain.repository.*;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

@Service
public class WorkService {

    @Autowired
    ObjectStorageApiClient objectStorageApiClient;
    @Autowired
    ProjectService projectService;
    @Autowired
    RequestService requestService;
    @Autowired
    ProblemRepository problemRepository;
    @Autowired
    ProblemRepositoryImpl problemRepositoryImpl;
    @Autowired
    CollectionWorkHistoryRepository collectionWorkHistoryRepository;
    @Autowired
    LabellingWorkHistoryRepository labellingWorkHistoryRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectRepositoryImpl projectRepositoryImpl;
    @Autowired
    ClassRepository classRepository;

    private int labellingWorkHistoryIdTurn;

    private int getLabellingWorkHistoryIdTurn() {
        int count = (int) labellingWorkHistoryRepository.count();
        return ++count;
    }

    // 교차검증 문제 생성 (작업자가 한 문제 풀 때마다 2개씩 생성)
    @Transactional
    protected void createCrossValidationProblem(int projectId, int problemId) {

        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if (problemEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("해당 문제의 교차검증 문제를 만들 수 없습니다.");
            return;
        }

        // 검증 대상 문제의 파일을 담은 bucketName
        String bucketName = problemEntityWrapper.get().getBucketName();
        // 검증 대상 문제의 파일 objectName
        String objectName = problemEntityWrapper.get().getObjectName();
        // 검증 대상 문제의 answer
        String answer = problemEntityWrapper.get().getAnswer();

        // 한 문제에 대한 교차검증 문제 2개만 만든다고 가정
        for(int i = 0; i < 2; i++) {
            int cvProblemId = projectService.getProblemIdTurn();
            // problemId, projectId, referenceId, bucketName, objectName, answer, finalAnswer, validationStatus, userId
            ProblemDto problemDto = new ProblemDto(cvProblemId, projectId, problemId,
                    bucketName, objectName, answer, "없음", "교차검증전", null);
            problemRepository.save(problemDto.toEntity());
        }

        return;
    }

    // 작업자가 수집한 데이터를 Object Storage에 업로드!
    private String uploadData(MultipartFile file, String bucketName) throws Exception {
        String fileName = file.getOriginalFilename();
        // 수정 포인트
        File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + fileName);
        file.transferTo(destinationFile);

        return objectStorageApiClient.putObject(bucketName, destinationFile);
    }

    // Object Storage에 수집한 데이터를 업로드 성공하면, 작업전 인 문제를 하나 찾아서 objectName을 저장하고 작업후로 상태 변경
    @Transactional
    protected int saveObjectName(int projectId, String objectName) {
        // 해당 수집 작업의 프로젝트 아이디로 파일 업로드 되지 않은 문제(작업전)를 찾음
        Optional<ProblemEntity> problemEntityWrapper =
                problemRepository.findFirstByProjectIdAndValidationStatus(projectId, "작업전");

        if(problemEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("프로젝트 아이디로 작업전 문제를 찾을 수 없음");
            return -1;
        }

        problemEntityWrapper.ifPresent(selectProblem -> {
            // 작업자가 업로드한 파일의 objectName 저장
            selectProblem.setObjectName(objectName);
            // 수집 작업전 -> 작업후
            selectProblem.setValidationStatus("작업후");

            problemRepository.save(selectProblem);
        });

        return problemEntityWrapper.get().getProblemId();
    }


    // 작업자의 수집 작업 기록
    @Transactional
    protected void saveCollectionWorkHistory(String userId, int problemId) {

        CollectionWorkHistoryDto collectionWorkHistoryDto = new CollectionWorkHistoryDto(problemId, userId);
        collectionWorkHistoryRepository.save(collectionWorkHistoryDto.toEntity());

    }


    public boolean collectionWork(String userId, int limit, MultipartHttpServletRequest uploadRequest,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {

        String className = requestService.getClassName(request);

        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");
        int numberOfData = labellingDataList.size();

        if (limit < 1) {
            System.out.println("========================");
            System.out.println("이미 필요한 데이터를 모두 수집");
            response.setHeader("upload", "fail - 이미 필요한 데이터를 모두 수집");
            return false;
        }

        int projectId = requestService.getProjectId(request);
        String bucketName = requestService.getBucketName(request);

        if (0 < numberOfData && numberOfData <= limit) {

            for(MultipartFile f : labellingDataList) {

                String objectName = uploadData(f, bucketName);

                if(objectName == null) {
                    System.out.println("========================");
                    System.out.println("Object Storage에 데이터 업로드 실패");
                    response.setHeader("upload", "fail - Object Storage에 데이터 업로드 실패");
                    return false;
                }

                // 해당 수집 작업의 프로젝트 아이디로 파일 업로드 되지 않은 문제(작업전)를 찾음
                // 작업자가 업로드한 파일의 objectName 저장
                // 수집 작업전 -> 작업후
                int problemId = saveObjectName(projectId, objectName);

                if(problemId != -1) {
                    if (saveAnswer(problemId, className, userId)) {
                        saveCollectionWorkHistory(userId, problemId);
                        // 교차검증 문제 만들기
                        createCrossValidationProblem(projectId, problemId);
                        continue;
                    }
                } else {
                    response.setHeader("upload", "fail - 프로젝트 아이디로 작업전 문제를 찾을 수 없음");
                    return false;
                }
            }
            response.setHeader("upload", "success");
            return true;
        }

        return false;
    }


    // 분류 작업을 시작하면 문제 한 세트(5개) 제공
    public List<ProblemDtoWithClassDto> provideClassificationProblems(String userId, HttpServletRequest request, HttpServletResponse response) {

        String dataType = requestService.getDataType(request);

        List<ProblemDtoWithClassDto> problemSet = combineProblems(dataType);

        if(problemSet.isEmpty()) {
            response.setHeader("problems", "fail");
            return null;
        }

        int historyId = saveLabellingWorkHistory(userId, dataType, problemSet);
        response.setHeader("workHistory", String.valueOf(historyId));
        response.setHeader("problems", "success");
        return problemSet;
    }

    // 사용자검증 문제 (1개) 생성하기 - DB
    @Transactional
    protected int createUserValidationProblem(int projectId, int problemId) {

        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        String bucketName = problemEntityWrapper.get().getBucketName();
        String objectName = problemEntityWrapper.get().getObjectName();
        String finalAnswer = problemEntityWrapper.get().getFinalAnswer();

        int uvProblemId = projectService.getProblemIdTurn();
        ProblemDto problemDto = new ProblemDto(uvProblemId, projectId, problemId,
                bucketName, objectName, "없음", finalAnswer, "사용자검증전", null);
        problemRepository.save(problemDto.toEntity());

        return uvProblemId;
    }

    // 사용자검증 문제 (1개) 생성하기
    private void userValidationProblems(List<ProblemEntity> selectedProblems) {

        Optional<ProblemEntity> userValidation = problemRepository
                .findByValidationStatus("검증완료");

        if(userValidation.isEmpty()) {
            System.out.println("========================");
            System.out.println("검증완료 문제를 찾을 수 없음");
            return;
        }

        int projectId = userValidation.get().getProjectId();
        int problemId = userValidation.get().getProblemId();
        // 검증완료된 문제로 또다시 사용자검증 문제를 만들어
        int uvProblemId = createUserValidationProblem(projectId, problemId);

        // 만들어진 사용자검증 문제의 problemId로 찾아
        Optional<ProblemEntity> userValidationProblem = problemRepository.findByProblemId(uvProblemId);

        if(userValidationProblem.isEmpty()) {
            System.out.println("========================");
            System.out.println("생성한 사용자검증 문제를 찾을 수 없음");
            return;
        }

        selectedProblems.add(userValidationProblem.get());

    }


    // 교차검증 문제(2개) 생성하기
    private void crossValidationProblems(List<ProblemEntity> selectedProblems) {

        List<ProblemEntity> crossValidationProblems =
                problemRepositoryImpl.crossValidation("작업후");
        selectedProblems.addAll(crossValidationProblems);

    }

    // 라벨링 문제(2개) 가져오기
    private void labellingProblems(String dataType, List<ProblemEntity> selectedProblems) {

        List<ProjectEntity> labellingProjects =
                projectRepositoryImpl.labellingProjectSearch("labelling", dataType);

        for(ProjectEntity p : labellingProjects) {
            int projectId = p.getProjectId();

            Optional<ProblemEntity> labellingProblem = problemRepository.findFirstByProjectIdAndValidationStatus(projectId, "작업전");
            selectedProblems.add(labellingProblem.get());
        }
    }

    private List<ProblemDtoWithClassDto> combineProblems(String dataType) {
        List<ProblemEntity> selectedProblems = new ArrayList<>();

        // 50개 랜덤으로 뽑음 - 테스트는 5개만 뽑을거임
        // 10개 (테스트 1개) = userValidation(검증완료)
        userValidationProblems(selectedProblems);
        // 20개 (테스트 2개) = crossValidation(작업후)
        crossValidationProblems(selectedProblems);
        // 20개 (테스트 2개) = labellingProblems(작업전)
        labellingProblems(dataType, selectedProblems);

        List<ProblemDto> problemSet = ObjectMapperUtils.mapAll(selectedProblems, ProblemDto.class);

        // 클래스 정보도 함께 주기 위해서 ProblemDto -> ProblemDtoWithClassDto 변환
        List<ProblemDtoWithClassDto> problemSetWithClassNames = withClassDtos(problemSet);
        return problemSetWithClassNames;
    }

    private List<ProblemDtoWithClassDto> withClassDtos(List<ProblemDto> problemDtos) {

        if(problemDtos.isEmpty())
            return null;

        List<ProblemDtoWithClassDto> results = new ArrayList<>();

        for(ProblemDto p : problemDtos) {

            int projectId = p.getProjectId();

            List<ClassEntity> classEntities = classRepository.findAllByProjectId(projectId);
            List<ClassDto> classNameList = ObjectMapperUtils.mapAll(classEntities, ClassDto.class);

            ProblemDtoWithClassDto pc = new ProblemDtoWithClassDto(p, classNameList);
            results.add(pc);
        }

        return results;
    }


    public void labellingWork(String userId, LinkedHashMap<String, Object> parameterMap,
                                 HttpServletRequest request, HttpServletResponse response) {

        int historyId = requestService.getHistoryId(request);

        LinkedHashMap<String, String> problemIdAnswerMap = new LinkedHashMap<>();
        for(String problemId : parameterMap.keySet()) {
            problemIdAnswerMap.put(problemId, parameterMap.get(problemId).toString());
        }

        // 문제 하나씩
        for(Map.Entry<String, String> entry : problemIdAnswerMap.entrySet()) {

            String strProblemId = entry.getKey();
            int problemId = Integer.parseInt(strProblemId);

            String answer = entry.getValue();

            if(!saveAnswer(problemId, answer, userId)) {
                // 답이 제대로 저장이 안되면 labellingWorkHistory 삭제 추가 ***
                labellingWorkHistoryRepository.deleteByHistoryId(historyId);
                System.out.println("========================");
                System.out.println("문제의 답을 저장할 수가 없음. 라벨링 작업 기록 삭제되었으니 작업 재시작 요망");
                response.setHeader("answer", "fail - 작업 다시 시작");
                return;
            }

            // 답이 제대로 저장이 되면 problem_table에서 해당 problem의 validation_status 변경
            updateValidationStatus(historyId, problemId);
        }

        response.setHeader("answer", "success");
        return;
    }


    @Transactional
    protected void updateValidationStatus(int historyId, int problemId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        Optional<LabellingWorkHistoryEntity> labellingWorkHistoryEntityWrapper
                = labellingWorkHistoryRepository.findByHistoryId(historyId);

        // problemId를 통해 이 문제가 userValidation인지 crossValidation인지 labellingProblem인지 알아내야 함
        // userValidation
        if(isUserValidation(historyId, problemId)) {
            problemEntityWrapper.ifPresent(selectProblem -> {
                selectProblem.setValidationStatus("사용자검증후");   // 사용자검증전 -> 사용자검증후
                problemRepository.save(selectProblem);
            });
        }

        // crossValidation
        else if(isCrossValidation(historyId, problemId)) {
            problemEntityWrapper.ifPresent(selectProblem -> {
                selectProblem.setValidationStatus("교차검증후");   // 교차검증전 -> 교차검증후
                problemRepository.save(selectProblem);
            });
        }

        // labellingProblem
        else {
            // 교차검증 문제 생성
            int projectId = problemEntityWrapper.get().getProjectId();
            createCrossValidationProblem(projectId, problemId);

            problemEntityWrapper.ifPresent(selectProblem -> {
                selectProblem.setValidationStatus("작업후");   // 작업전 -> 작업후
                problemRepository.save(selectProblem);
            });
        }
    }


    // 야매
    private boolean isUserValidation(int historyId, int problemId) {
        Optional<LabellingWorkHistoryEntity> labellingWorkHistoryEntityWrapper
                = labellingWorkHistoryRepository.findByHistoryId(historyId);

        if (labellingWorkHistoryEntityWrapper.get().getUv1() == problemId) {
            return true;
        }

        return false;
    }

    // 야매
    private boolean isCrossValidation(int historyId, int problemId) {
        Optional<LabellingWorkHistoryEntity> labellingWorkHistoryEntityWrapper
                = labellingWorkHistoryRepository.findByHistoryId(historyId);

        if (labellingWorkHistoryEntityWrapper.get().getCv1() == problemId) {
            return true;
        } else if(labellingWorkHistoryEntityWrapper.get().getCv2() == problemId) {
            return true;
        }

        return false;
    }

    @Transactional
    protected boolean saveAnswer(int problemId, String answer, String userId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if(problemEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("problemEntityWrapper.isEmpty()");
            return false;
        }

        problemEntityWrapper.ifPresent(selectProblem -> {

            // 수집/분류 작업은 작업자가 선택한 className을 answer에 저장
            // 바운딩박스 작업은 ..
            selectProblem.setAnswer(answer);
            // 작업자의 userID를 저장
            selectProblem.setUserId(userId);

            problemRepository.save(selectProblem);
        });

        return true;
    }

    @Transactional
    protected int saveLabellingWorkHistory(String userId, String dataType, List<ProblemDtoWithClassDto> problems) { // 5개만

        labellingWorkHistoryIdTurn = getLabellingWorkHistoryIdTurn();
        int historyId = this.labellingWorkHistoryIdTurn;

        LabellingWorkHistoryDto labellingWorkHistoryDto = new LabellingWorkHistoryDto(historyId, userId, dataType
                , problems.get(0).getProblemDto().getProblemId()
                , problems.get(1).getProblemDto().getProblemId()
                , problems.get(2).getProblemDto().getProblemId()
                , problems.get(3).getProblemDto().getProblemId()
                , problems.get(4).getProblemDto().getProblemId());

        if(labellingWorkHistoryRepository.save(labellingWorkHistoryDto.toEntity()) == null)
            return -1;

        return historyId;
    }


    // 본인이 작업한 목록 확인
    public List<WorkHistoryDto> getWorkList(String userId, HttpServletResponse response) {

        List<WorkHistoryDto> workList = new ArrayList<>();
        getCollectionWorkList(userId, workList);
        getLabellingWorkList(userId, workList);

        if(workList == null) {
            response.setHeader("workList", "fail");
            return null;
        }

        response.setHeader("workList", "success");
        return workList;
    }

    // 수집 작업 목록 확인
    private void getCollectionWorkList(String userId, List<WorkHistoryDto> workList) {
        List<CollectionWorkHistoryEntity> collectionWorkHistoryEntities
                = collectionWorkHistoryRepository.findAllByUserId(userId);

        if(collectionWorkHistoryEntities == null)
            return;

        for(CollectionWorkHistoryEntity c : collectionWorkHistoryEntities) {
            int problemId = c.getProblemId();
            WorkHistoryDto w = createWorkHistoryDto(problemId);
            if(w == null)
                continue;
            workList.add(w);
        }

    }

    // 라벨링 작업 목록 확인
    private void getLabellingWorkList(String userId, List<WorkHistoryDto> workList) {
        List<LabellingWorkHistoryEntity> labellingWorkHistoryEntities
                = labellingWorkHistoryRepository.findAllByUserId(userId);

        if(labellingWorkHistoryEntities == null)
            return;

        for(LabellingWorkHistoryEntity l : labellingWorkHistoryEntities) {
            // 사용자 검증 문제 1개는 필요없음 - 포인트 미지급 대상

            int problems[] = new int[4];

            // 교차검증 문제 2개
            problems[0] = l.getCv1();
            problems[1] = l.getCv2();

            // 라벨링 문제 2개
            problems[2] = l.getLp1();
            problems[3] = l.getLp2();

            for(int i = 0; i < problems.length; i++) {
                int problemId = problems[i];
                WorkHistoryDto w = createWorkHistoryDto(problemId);
                workList.add(w);
            }

        }
    }

    // problemId -> workHistoryDto 생성
    private WorkHistoryDto createWorkHistoryDto(int problemId) {
        Optional<ProblemEntity> problemEntity = problemRepository.findByProblemId(problemId);

        if (problemEntity.get() == null)
            return null;

        int projectId = problemEntity.get().getProjectId();
        Optional<ProjectEntity> projectEntity = projectRepository.findByProjectId(projectId);

        if (projectEntity.isPresent()) {
            String projectRequester = projectEntity.get().getUserId();
            String projectName = projectEntity.get().getProjectName();
            String projectWorkType = projectEntity.get().getWorkType();
            String projectDataType = projectEntity.get().getDataType();
            String projectStatus = projectEntity.get().getStatus();

            WorkHistoryDto workHistoryDto = new WorkHistoryDto(projectRequester, projectName, projectWorkType,
                    projectDataType, projectStatus, problemId);

            return workHistoryDto;
        }

        return null;
    }
}
