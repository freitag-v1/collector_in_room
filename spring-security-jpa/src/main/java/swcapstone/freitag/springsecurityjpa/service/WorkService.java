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

    protected List<ProblemDtoWithClassDto> withClassDtos(List<ProblemDto> problemDtos) {

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

    // 교차검증 문제(2개) 생성하기
    protected void crossValidationProblems(List<ProblemEntity> selectedProblems) {

        List<ProblemEntity> crossValidationProblems =
                problemRepositoryImpl.crossValidation("교차검증전");
        selectedProblems.addAll(crossValidationProblems);

    }

    // 교차검증 문제 생성 (작업자가 한 문제 풀 때마다 2개씩 생성)
    @Transactional
    protected void createCrossValidationProblem(int projectId, int problemId) {

        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if (problemEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("해당 문제의 교차검증 문제를 만들 수 없음");
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
