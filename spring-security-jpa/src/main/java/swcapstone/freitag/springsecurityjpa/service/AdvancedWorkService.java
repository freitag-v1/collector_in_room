package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProblemRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class AdvancedWorkService {

    @Autowired
    ProblemRepository problemRepository;

    private static final int NUM_OF_ANSWERS = 3;

    // 검증완료 된 문제 개수 몇개인지 확인
    protected int howManyValidated(int projectId) {

        List<ProblemEntity> problemEntities = problemRepository.findAllByProjectId(projectId);
        int count = 0;

        for(ProblemEntity p : problemEntities) {
            if (p.getValidationStatus().equals("검증완료"))
                count++;
        }

        return count;
    }

    // 교차검증
    @Transactional
    protected void crossValidateProblem(int referenceId) {

        List<ProblemEntity> crossValidationProblems
                = problemRepository.findAllByReferenceIdAndValidationStatus(referenceId, "교차검증후");

        if (crossValidationProblems.isEmpty()) {
            System.out.println("========================");
            System.out.println("아무도 교차검증에 참여하지 않음");
            return;
        } else if (crossValidationProblems.size() < 2) {
            System.out.println("========================");
            System.out.println("교차검증에 참여한 작업자 수 미달");
            return;
        }

        Optional<ProblemEntity> originalProblem = problemRepository.findByProblemId(referenceId);

        if (originalProblem.isEmpty()) {
            System.out.println("========================");
            System.out.println("교차검증 대상인 문제를 찾을 수 없음");
            return;
        } else if (originalProblem.get().getValidationStatus().equals("작업전")) {
            System.out.println("========================");
            System.out.println("교차검증 대상인 문제가 아직 작업전");
            return;
        }

        String[] answers = new String[NUM_OF_ANSWERS];

        for (int i = 0; i < answers.length; i++) {
            if (i == 0) {
                answers[i] = originalProblem.get().getAnswer();
            } else {
                answers[i] = crossValidationProblems.get(i - 1).getAnswer();
            }
        }

        String finalAnswer = findFinalAnswer(answers);

        if (finalAnswer.equals("없음")) {
            return;
        } else {

            originalProblem.ifPresent(selectProblem -> {
                selectProblem.setFinalAnswer(finalAnswer);
                selectProblem.setValidationStatus("검증완료");
                problemRepository.save(selectProblem);
            });

            for (ProblemEntity p : crossValidationProblems) {
                int problemId = p.getProblemId();

                Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

                problemEntityWrapper.ifPresent(selectProblem ->
                {
                    selectProblem.setFinalAnswer(finalAnswer);
                    problemRepository.save(selectProblem);
                });
            }
        }
    }

    // Voting!
    private static String findFinalAnswer(String[] answers) {

        int count = 0;
        String candidate = null;

        for (int i = 0; i < answers.length; i++) {
            if (count == 0) {
                candidate = answers[i];
                count = 1;
                continue;
            } else if (candidate.equals(answers[i]))
                count++;
            else {
                count--;
            }
        }

        String finalAnswer = "없음";

        if (count == 0) {
            System.out.println("========================");
            System.out.println("기존 작업자(1명)와 교차검증 참여 작업자(2명) 모두 다른 답을 함");
            return finalAnswer;
        } else {
            count = 0;
            for (int i = 0; i < answers.length; i++) {
                if (candidate.equals(answers[i]))
                    count++;
            }

            if (count > answers.length / 2) {
                System.out.println("========================");
                System.out.println("Final answer : " + candidate);
                finalAnswer = candidate;
                return finalAnswer;
            }

        }

        System.out.println("========================");
        System.out.println("Final answer가 여러 개이므로 교차검증 작업자가 더 필요함");
        return finalAnswer;
    }
}
