package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProblemService extends WorkService {

    // 사용자검증 문제 (1개) 생성하기
    @Transactional
    protected int createUserValidationProblem(int projectId, int problemId) {

        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        String bucketName = problemEntityWrapper.get().getBucketName();
        String objectName = problemEntityWrapper.get().getObjectName();
        String finalAnswer = problemEntityWrapper.get().getFinalAnswer();

        int uvProblemId = projectService.getProblemIdTurn();
        ProblemDto problemDto = new ProblemDto(uvProblemId, projectId, problemId,
                bucketName, objectName, "없음", finalAnswer, "사용자검증전", null, null);
        problemRepository.save(problemDto.toEntity());

        return uvProblemId;
    }

    // 사용자검증 문제 (1개) 제공하기
    @Transactional
    protected void userValidationProblem(List<ProblemEntity> selectedProblems) {

        List<ProblemEntity> userValidation = problemRepositoryImpl.userValidation("검증완료", 1);

        if(userValidation.isEmpty()) {
            System.out.println("========================");
            System.out.println("검증완료 문제를 찾을 수 없음");
            return;
        }

        int projectId = userValidation.get(0).getProjectId();
        int problemId = userValidation.get(0).getProblemId();
        // 검증완료된 문제로 또다시 사용자검증 문제를 만들어
        int uvProblemId = createUserValidationProblem(projectId, problemId);

        System.out.println("========================");
        System.out.println("uvProblemId: " + uvProblemId);

        // 만들어진 사용자검증 문제의 problemId로 찾아
        Optional<ProblemEntity> userValidationProblem = problemRepository.findByProblemId(uvProblemId);

        if(userValidationProblem.isEmpty()) {
            System.out.println("========================");
            System.out.println("생성한 사용자검증 문제를 찾을 수 없음");
            return;
        }

        userValidationProblem.ifPresent(selectProblem -> {
            selectProblem.setValidationStatus("사용자검증중");
            problemRepository.save(selectProblem);
        });

        selectedProblems.add(userValidationProblem.get());
    }

    // 분류 문제(2개) 가져오기
    @Transactional
    protected void labellingProblems(List<ProblemEntity> selectedProblems, String level) {

        if (projectRepository.countByWorkTypeAndDataTypeAndStatus("labelling", "classification", "진행중") > 1) {
            List<ProjectEntity> labellingProjects =
                    projectRepositoryImpl.labellingProjectSearch("labelling", "classification", 2);

            for(ProjectEntity p : labellingProjects) {
                int projectId = p.getProjectId();

                List<ProblemEntity> labellingProblem
                        = problemRepositoryImpl.labellingProblem(projectId, "작업전", 1);

                if (labellingProblem.isEmpty()) {
                    System.out.println("========================");
                    System.out.println("라벨링 문제를 가져올 수 없음");
                    return;
                }

                labellingProblem.get(0).setValidationStatus("작업중");
                labellingProblem.get(0).setLevel(level);
                problemRepository.save(labellingProblem.get(0));
                selectedProblems.add(labellingProblem.get(0));
            }

            System.out.println("========================");
            System.out.println("교차검증 문제 각각은 서로 다른 프로젝트에서 가져옴");
            return;
        }

        List<ProjectEntity> labellingProject =
                projectRepositoryImpl.labellingProjectSearch("labelling", "classification", 1);

        if (labellingProject.isEmpty()) {
            System.out.println("========================");
            System.out.println("라벨링 분류 프로젝트를 찾을 수가 없음");
            return;
        }

        int projectId = labellingProject.get(0).getProjectId();
        List<ProblemEntity> labellingProblems
                = problemRepositoryImpl.labellingProblem(projectId, "작업전", 2);

        for (ProblemEntity p : labellingProblems) {
            p.setValidationStatus("작업중");
            p.setLevel(level);
            problemRepository.save(p);
        }

        selectedProblems.addAll(labellingProblems);

    }

    protected List<ProblemDto> combineProblems(String userId) {

        String level = getLevel(userId);
        List<ProblemEntity> selectedProblems = new ArrayList<>();

        // 50개 랜덤으로 뽑음 - 테스트는 5개만 뽑을거임
        // 10개 (테스트 1개) = userValidation(검증완료)
        userValidationProblem(selectedProblems);
        // 20개 (테스트 2개) = crossValidation(작업후)
        crossValidationProblems(selectedProblems, level);
        // 20개 (테스트 2개) = labellingProblems(작업전)
        labellingProblems(selectedProblems, level);

        List<ProblemDto> problemSet = ObjectMapperUtils.mapAll(selectedProblems, ProblemDto.class);
        return problemSet;
    }
}
