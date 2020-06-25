package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.*;
import swcapstone.freitag.springsecurityjpa.domain.repository.*;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletResponse;
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
    BoundingBoxRepository boundingBoxRepository;
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
    @Autowired
    UserRepository userRepository;


    protected List<ProblemDtoWithClassDto> withClassDtos(List<ProblemDto> problemDtos) {

        if(problemDtos.isEmpty())
            return null;

        List<ProblemDtoWithClassDto> results = new ArrayList<>();

        for(ProblemDto p : problemDtos) {

            int projectId = p.getProjectId();

            Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);
            if (projectEntityWrapper.isEmpty()) {
                System.out.println("========================");
                System.out.println("projectEntityWrapper.isEmpty()");
                return null;
            }

            List<ClassEntity> classEntities = classRepository.findAllByProjectId(projectId);
            List<ClassDto> classNameList = ObjectMapperUtils.mapAll(classEntities, ClassDto.class);
            String conditionContent = projectEntityWrapper.get().getConditionContent();

            ProblemDtoWithClassDto pc = new ProblemDtoWithClassDto(p, classNameList, conditionContent);
            results.add(pc);
        }

        return results;
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
            selectProblem.setAnswer(answer);
            // 작업자의 userID를 저장
            selectProblem.setUserId(userId);

            // 사용자 검증 문제는 바로 결과를 냄
            if (selectProblem.getAnswer().equals(selectProblem.getFinalAnswer())) {
                selectProblem.setRightAnswer(true);
            }

            problemRepository.save(selectProblem);
        });

        return true;
    }


    protected void withBoundingBoxDtos(List<ProblemDtoWithClassDto> problemDtoWithClassDtos) {

        for(int i = 1; i < 3; i++) {
            int projectId = problemDtoWithClassDtos.get(i).getProblemDto().getProjectId();
            int referenceId = problemDtoWithClassDtos.get(i).getProblemDto().getReferenceId();
            List<BoundingBoxDto> boundingBoxDtos = isBoundingBoxWork(projectId, referenceId);

            if(boundingBoxDtos == null)
                continue;
            else {
                problemDtoWithClassDtos.get(i).setBoundingBoxList(boundingBoxDtos);
                problemDtoWithClassDtos.set(i, problemDtoWithClassDtos.get(i));
            }
        }
    }


    private List<BoundingBoxDto> isBoundingBoxWork(int projectId, int referenceId) {

        Optional<ProjectEntity> projectEntity = projectRepository.findByProjectId(projectId);

        if(projectEntity.isEmpty())
            return null;

        String dataType = projectEntity.get().getDataType();
        if(dataType.equals("boundingBox")) {
            List<BoundingBoxEntity> boundingBoxEntities = boundingBoxRepository.findAllByProblemId(referenceId);

            if(boundingBoxEntities.isEmpty()) {
                System.out.println("========================");
                System.out.println("boundingBoxEntities.isEmpty()");
                return null;
            }

            return ObjectMapperUtils.mapAll(boundingBoxEntities, BoundingBoxDto.class);
        }

        return null;
    }

    // 교차검증 문제(2개) 생성하기
    @Transactional
    protected void crossValidationProblems(List<ProblemEntity> selectedProblems, String level) {

        List<ProblemEntity> crossValidationProblems = new ArrayList<>();
        // 이 level의 작업자가 참여할 수 있는 교차검증 문제 개수
        int numOfProblems = (int) problemRepository.countByValidationStatusAndLevel("교차검증전", level);

        if (numOfProblems == 1) {
            // ex) 하나는 슈퍼작업자, 하나는 상
            crossValidationProblems.add(problemRepositoryImpl.crossValidations("교차검증전", level, 1).get(0));

            if (level.equals("슈퍼작업자")) {
                level = level.replaceAll(level, "상");
                System.out.println("========================");
                System.out.println("슈퍼작업자 -> " + level);
            }
            else if (level.equals("상")) {
                level = level.replaceAll(level, "중");
            }
            else if (level.equals("중")) {
                level = level.replaceAll(level, "하");
            }
            else {
                System.out.println("========================");
                System.out.println("띠용!");
                return;
            }

            System.out.println("========================");
            System.out.println("[교차검증문제1개선택후] level : " + level);

            crossValidationProblems.add(problemRepositoryImpl.crossValidations("교차검증전", level, 1).get(0));

        } else {
            level = level.replaceAll(level, "상");
            System.out.println("========================");
            System.out.println("슈퍼작업자 -> " + level);
            crossValidationProblems.addAll(problemRepositoryImpl.crossValidations("교차검증전", level, 2));
        }

        for (ProblemEntity p : crossValidationProblems) {
            p.setValidationStatus("교차검증중");
            problemRepository.save(p);
        }

        selectedProblems.addAll(crossValidationProblems);
    }

    // 교차검증 문제 생성 (작업자가 한 문제 풀 때마다 2개씩 생성)
    @Transactional
    protected void createCrossValidationProblem(int problemId) {

        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if (problemEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("해당 문제의 교차검증 문제를 만들 수 없음");
            return;
        }

        // 검증 대상 문제의 projectId
        int projectId = problemEntityWrapper.get().getProjectId();
        // 검증 대상 문제의 파일을 담은 bucketName
        String bucketName = problemEntityWrapper.get().getBucketName();
        // 검증 대상 문제의 파일 objectName
        String objectName = problemEntityWrapper.get().getObjectName();
        // 검증 대상 문제의 level
        String level = problemEntityWrapper.get().getLevel();

        String[] levelList;
        if (level.equals("상"))
            levelList = new String[]{"상", "중", "중", "하"};
        else if (level.equals("중"))
            levelList = new String[]{"상", "상", "중", "하"};
        else
            levelList = new String[]{"상", "상", "중", "중"};

        if (levelList.length != 4) {
            System.out.println("========================");
            System.out.println("등급 구성 에러 발생함");
            return;
        }

        // 한 문제에 대한 교차검증 문제 2개만 만든다고 가정
        for(int i = 0; i < 4; i++) {
            int cvProblemId = projectService.getProblemIdTurn();
            // problemId, projectId, referenceId, bucketName, objectName, answer, finalAnswer, validationStatus, userId, level
            ProblemDto problemDto = new ProblemDto(cvProblemId, projectId, problemId,
                    bucketName, objectName, "없음", "없음", "교차검증전", null, levelList[i]);
            problemRepository.save(problemDto.toEntity());
        }
    }

    // 교차검증에 참여하는 작업자의 정확도 계산
    private double calculateAccuracy(String userId) {

        double solvedProblems = problemRepository.countByUserIdAndValidationStatus(userId, "검증완료");
        double rightProblems = problemRepository.countByUserIdAndValidationStatusAndRightAnswer(userId, "검증완료", true);

        if (solvedProblems == 0)
            return 0;

        double userAccuracy = rightProblems / solvedProblems;

        System.out.println("========================");
        System.out.println("rightProblems : " + rightProblems);
        System.out.println("solvedProblems : " + solvedProblems);
        System.out.println("userAccuracy : " + userAccuracy);

        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(selectUser -> {
            selectUser.setUserAccuracy(userAccuracy);
            userRepository.save(selectUser);
        });

        // 정확도가 100%이면서 맞춘 문제가 10개 이상이면 슈퍼작업자
        if (userAccuracy == 1 && solvedProblems >= 10)
            return 1.1;

        return userAccuracy;
    }

    // 교차검증에 참여하는 작업자의 레벨 산출
    protected String getLevel(String userId) {

        double userAccuracy = calculateAccuracy(userId);

        if (userAccuracy == 1.1)
            return "슈퍼작업자";
        else if (userAccuracy >= 0.9)
            return "상";
        else if (userAccuracy >= 0.8)
            return "중";
        else
            return "하";
    }

    @Transactional
    protected void updateLevel(String userId, int problemId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        String level = getLevel(userId);

        System.out.println("========================");
        System.out.println(userId + "님의 level은 " + level);

        problemEntityWrapper.ifPresent(selectProblem -> {
            selectProblem.setLevel(level);
            problemRepository.save(selectProblem);
        });
    }

    // 본인이 작업한 목록 확인
    public List<WorkHistoryDto> getWorkList(String userId, HttpServletResponse response) {

        List<WorkHistoryDto> workList = new ArrayList<>();
        getCollectionWorkList(userId, workList);
        getLabellingWorkList(userId, workList);

        if(workList.isEmpty()) {
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
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if (problemEntityWrapper.isPresent()) {

            String validationStatus = problemEntityWrapper.get().getValidationStatus();

            int projectId = problemEntityWrapper.get().getProjectId();
            Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

            String projectRequester = "";
            String projectName = "";
            String projectWorkType = "";
            String projectDataType = "";

            if (projectEntityWrapper.isPresent()) {
                projectRequester += projectEntityWrapper.get().getUserId();
                projectName += projectEntityWrapper.get().getProjectName();
                projectWorkType += projectEntityWrapper.get().getWorkType();
                projectDataType += projectEntityWrapper.get().getDataType();
            } else {
                return null;
            }

            String problemStatus = mappingStatus(validationStatus);

            WorkHistoryDto workHistoryDto = new WorkHistoryDto(projectRequester, projectName, projectWorkType,
                    projectDataType, problemStatus, problemId);

            return workHistoryDto;
        }

        return null;
    }

    private String mappingStatus(String validationStatus) {

        if (validationStatus.isEmpty())
            return null;

        if (validationStatus.equals("검증완료")) {
            return "포인트지급 완료";
        } else if (validationStatus.equals("작업후") ||
                validationStatus.equals("교차검증후") ||
                validationStatus.equals("검증대기")) {
            return "포인트지급 대기";
        } else {
            return "";
        }
    }

    // 포인트 지급
    @Transactional
    protected void payPoints(int problemId, String userId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);

        problemEntityWrapper.ifPresent(selectProblem -> {
            String validationStatus = selectProblem.getValidationStatus();

            // 작업 기본 비용 지급 - 10원?
            if (validationStatus.equals("작업후") || validationStatus.equals("교차검증후")) {
                userEntityWrapper.ifPresent(selectUser -> {
                    int point = selectUser.getPoint();
                    int totalPoint = selectUser.getTotalPoint();

                    selectUser.setPoint(point + 10);
                    selectUser.setTotalPoint(totalPoint + 10);

                    userRepository.save(selectUser);
                });
            } else if (validationStatus.equals("검증완료")) {
                // 포인트 차등 지급 ??
            }
        });
    }
}
