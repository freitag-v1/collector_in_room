package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.dto.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.BoundingBoxEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.LabellingWorkHistoryEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class ClassificationWorkService extends WorkService {

    @Autowired
    ProblemService problemService;

    private int getLabellingWorkHistoryIdTurn() {
        Optional<LabellingWorkHistoryEntity> labellingWorkHistoryEntityWrapper =
                labellingWorkHistoryRepository.findTopByOrderByIdDesc();

        if (labellingWorkHistoryEntityWrapper.isEmpty())
            return 1;

        return labellingWorkHistoryEntityWrapper.get().getHistoryId() + 1;
    }

    protected int getBoxIdTurn() {
        Optional<BoundingBoxEntity> boundingBoxEntityWrapper = boundingBoxRepository.findTopByOrderByIdDesc();

        if (boundingBoxEntityWrapper.isEmpty())
            return 1;

        return boundingBoxEntityWrapper.get().getBoxId() + 1;
    }

    @Transactional
    protected int saveLabellingWorkHistory(String userId, String dataType, List<ProblemDto> problems) { // 5개만

        if (problems.size() != 5) {
            System.out.println("========================");
            System.out.println("IndexOutOfBoundsException");
            return -1;
        }

        int historyId = getLabellingWorkHistoryIdTurn();

        LabellingWorkHistoryDto labellingWorkHistoryDto = new LabellingWorkHistoryDto(historyId, userId, dataType
                , problems.get(0).getProblemId()
                , problems.get(1).getProblemId()
                , problems.get(2).getProblemId()
                , problems.get(3).getProblemId()
                , problems.get(4).getProblemId());

        if(labellingWorkHistoryRepository.save(labellingWorkHistoryDto.toEntity()) == null)
            return -1;

        return historyId;
    }


    // 분류 작업을 시작하면 문제 한 세트(5개) 제공
    public List<ProblemDtoWithClassDto> provideClassificationProblems(String userId, HttpServletResponse response) {

        List<ProblemDto> problemSet = problemService.combineProblems(userId);

        if (problemSet.isEmpty()) {
            System.out.println("========================");
            System.out.println("problemSet.isEmpty()");
            response.setHeader("problems", "fail");
            return null;
        }

        if(problemSet.size() != 5) {
            System.out.println("========================");
            System.out.println("problemSet.size() != 5");
            response.setHeader("problems", "fail");
            return null;
        }

        // 클래스 정보도 함께 주기 위해서 ProblemDto -> ProblemDtoWithClassDto 변환
        List<ProblemDtoWithClassDto> problemSetWithClassNames = withClassDtos(problemSet);
        // 교차검증 문제가 바운딩박스 문제 기반으로 만들어졌는가?
        withBoundingBoxDtos(problemSetWithClassNames);

        int historyId = saveLabellingWorkHistory(userId, "classification", problemSet);

        response.setHeader("workHistory", String.valueOf(historyId));
        response.setHeader("problems", "success");
        return problemSetWithClassNames;
    }


    // 분류 작업
    public boolean classificationWork(String userId, LinkedHashMap<String, Object> parameterMap,
                                      HttpServletRequest request, HttpServletResponse response) {

        int historyId = requestService.getHistoryIdH(request);

        if(parameterMap.size() != 5) {
            // 답이 제대로 안오면 labellingWorkHistory 삭제 추가 ***
            labellingWorkHistoryRepository.deleteByHistoryId(historyId);
            System.out.println("========================");
            System.out.println("문제의 답이 5개가 아님. 라벨링 작업 기록 삭제되었으니 작업 재시작 요망");
            response.setHeader("answer", "send answers again");
            return false;
        }

        LinkedHashMap<String, String> problemIdAnswerMap = new LinkedHashMap<>();

        for(String problemId : parameterMap.keySet()) {
            problemIdAnswerMap.put(problemId, parameterMap.get(problemId).toString());
        }

        // 문제 하나씩
        for(Map.Entry<String, String> entry : problemIdAnswerMap.entrySet()) {

            String strProblemId = entry.getKey();
            int problemId = Integer.parseInt(strProblemId);

            String answer = entry.getValue();

            if(saveAnswer(problemId, answer, userId)) {
                // 답이 제대로 저장이 되면 problem_table에서 해당 problem의 validation_status 변경
                updateValidationStatus(historyId, problemId);
            } else {
                // 답이 제대로 저장이 안되면 labellingWorkHistory 삭제 추가 ***
                labellingWorkHistoryRepository.deleteByHistoryId(historyId);
                System.out.println("========================");
                System.out.println("문제의 답을 저장할 수가 없음. 라벨링 작업 기록 삭제되었으니 작업 재시작 요망");
                response.setHeader("answer", "fail - 작업 다시 시작");
                return false;
            }

        }

        return true;
    }

    @Transactional
    protected void updateValidationStatus(int historyId, int problemId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if (problemEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("문제를 찾을 수 없음");
            return;
        }

        // problemId를 통해 이 문제가 userValidation인지 crossValidation인지 labellingProblem인지 알아내야 함
        // userValidation - 사용자검증전, 사용자검증중, 검증완료
        if(isUserValidation(historyId, problemId)) {
            problemEntityWrapper.ifPresent(selectProblem -> {
                selectProblem.setValidationStatus("검증완료");   // 사용자검증중 -> 검증완료
                problemRepository.save(selectProblem);
            });
        }

        // crossValidation - 교차검증전, 교차검증중, 교차검증후, 검증완료
        else if(isCrossValidation(historyId, problemId)) {
            problemEntityWrapper.ifPresent(selectProblem -> {
                selectProblem.setValidationStatus("교차검증후");   // 교차검증중 -> 교차검증후
                problemRepository.save(selectProblem);
            });

            int referenceId = problemEntityWrapper.get().getReferenceId();
            crossValidateProblem(referenceId);
        }

        // labellingProblem - 작업전, 작업중, 작업후, (검증대기), 검증완료
        else {

            problemEntityWrapper.ifPresent(selectProblem -> {
                selectProblem.setValidationStatus("작업후");   // 작업중 -> 작업후
                problemRepository.save(selectProblem);
            });

            // 교차검증 문제 생성
            createCrossValidationProblem(problemId);

            int projectId = problemEntityWrapper.get().getProjectId();
            projectService.setProgressData(projectId, 1);
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

    // 교차검증
    @Transactional
    protected void crossValidateProblem(int referenceId) {

        List<ProblemEntity> crossValidationProblems
                = problemRepository.findAllByReferenceIdAndValidationStatus(referenceId, "교차검증후");

        if (crossValidationProblems.isEmpty()) {
            System.out.println("========================");
            System.out.println("아무도 교차검증에 참여하지 않음");
            return;
        } else if (crossValidationProblems.size() < 3) {
            System.out.println("========================");
            System.out.println("교차검증에 참여한 작업자 수 미달");
            return;
        }

        // 교차검증 대상 문제
        Optional<ProblemEntity> originalProblem = problemRepository.findByProblemId(referenceId);

        if (originalProblem.isEmpty()) {
            System.out.println("========================");
            System.out.println("교차검증 대상인 문제를 찾을 수 없음");
            return;
        }

        int projectId = originalProblem.get().getProjectId();
        int size = crossValidationProblems.size() + 1;

        String answers[] = new String[size];
        String workers[] = new String[size];

        for (int i = 0; i < size; i++) {
            if(i == 0) {
                answers[i] = originalProblem.get().getAnswer();
                workers[i] = originalProblem.get().getUserId();
            } else {
                answers[i] = crossValidationProblems.get(i - 1).getAnswer();
                workers[i] = crossValidationProblems.get(i - 1).getUserId();
            }
        }

        String levelList[] = new String[size];

        for (int i = 0; i < size; i++) {
            levelList[i] = getLevel(workers[i]);
        }

        Optional<ProjectEntity> projectEntityWrapper
                = projectRepository.findByProjectId(projectId);

        if (projectEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("DB 에러 - 프로젝트 찾을 수 없음");
            return;
        }

        String finalAnswer = findFinalAnswer(answers, levelList);

        // Voting을 할 수 없는 경우
        if (finalAnswer == null) {
            return;
        }
        // Voting을 통해 최종 답이 나온 경우
        else {
            originalProblem.ifPresent(selectProblem -> {
                selectProblem.setFinalAnswer(finalAnswer);
                selectProblem.setValidationStatus("검증완료");  // 작업후 -> 검증완료

                if (selectProblem.getAnswer().equals(selectProblem.getFinalAnswer()))
                    selectProblem.setRightAnswer(true);

                problemRepository.save(selectProblem);
            });

            for (ProblemEntity p : crossValidationProblems) {
                int problemId = p.getProblemId();

                Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

                problemEntityWrapper.ifPresent(selectProblem ->
                {
                    selectProblem.setFinalAnswer(finalAnswer);
                    selectProblem.setValidationStatus("검증완료");  // 교차검증후 -> 검증완료

                    if (selectProblem.getAnswer().equals(selectProblem.getFinalAnswer()))
                        selectProblem.setRightAnswer(true);

                    problemRepository.save(selectProblem);
                });
            }

            // validatedData++
            Optional<ProjectEntity> targetProject = projectRepository.findByProjectId(projectId);

            targetProject.ifPresent(selectProject -> {
                int validatedData = selectProject.getValidatedData();
                validatedData += 1;
                selectProject.setValidatedData(validatedData);

                projectRepository.save(selectProject);
            });

            // 난이도 -> difficulty 게산

        }
    }

    // Voting!
    private static String findFinalAnswer(String[] answers, String[] levelList) {
        String candidate = boyerMooreMajorityVote(answers, levelList);
        if(candidate == null) {
            return mostFrequent(answers, levelList);
        } else {
            return candidate;
        }
    }

    private static String boyerMooreMajorityVote(String[] answers, String[] levelList) {
        String candidate = null;
        int voted = 0;

        // first pass - 과반수 표를 받은 후보
        for(int i = 0; i < answers.length; i++) {
            // 유저마다 기존 정확도에 따라 가중치를 가짐
            int num = getWeight(levelList[i]);
            for(int j = 0; j < num; j++) {
                if(voted == 0) {
                    candidate = answers[i];
                    voted++;
                } else if(candidate.equals(answers[i])) {
                    voted++;
                } else {
                    voted--;
                }
            }
        }

        // second pass - 과반수 검증
        voted = 0;
        for(int i = 0; i < answers.length; i++) {
            // 유저마다 기존 정확도에 따라 가중치를 가짐
            int num = getWeight(levelList[i]);
            for(int j = 0; j < num; j++) {
                if(candidate.equals(answers[i])) {
                    voted++;
                }
            }
        }

        if(6 <= voted) {
            // 과반수 득표
            return candidate;
        } else {
            return null;
        }
    }

    private static String mostFrequent(String[] answers, String[] levelList) {
        TreeMap<String, Integer> voted = new TreeMap<>();

        for(int i = 0; i < answers.length; i++) {
            if(voted.containsKey(answers[i])) {
                voted.put(answers[i], voted.get(answers[i]) + getWeight(levelList[i]));
            } else {
                voted.put(answers[i], getWeight(levelList[i]));
            }
        }

        Map.Entry<String, Integer> candidate = voted.firstEntry();
        int count = 0;
        for(Map.Entry<String, Integer> entry : voted.entrySet()) {
            if(candidate.getValue() < entry.getValue()) {
                candidate = entry;
                count = 1;
            } else if(candidate.getValue() == entry.getValue()) {
                count++;
            }
        }

        if(count == 1) {
            return candidate.getKey();
        } else {
            return null;
        }
    }

    private static int getWeight(String level) {
        int num = 0;
        if ("상".equals(level)) {
            num = 3;
        } else if ("중".equals(level)) {
            num = 2;
        } else if ("하".equals(level)) {
            num = 1;
        }
        return num;
    }

}
