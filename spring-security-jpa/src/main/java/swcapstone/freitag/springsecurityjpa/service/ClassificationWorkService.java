package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.dto.LabellingWorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDtoWithClassDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.LabellingWorkHistoryEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProblemRepository;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class ClassificationWorkService extends WorkService {

    @Autowired
    ProblemRepository problemRepository;

    private static final int NUM_OF_ANSWERS = 3;

    private int labellingWorkHistoryIdTurn;

    private int getLabellingWorkHistoryIdTurn() {
        int count = (int) labellingWorkHistoryRepository.count();
        return ++count;
    }

    @Transactional
    protected int saveLabellingWorkHistory(String userId, String dataType, List<ProblemDtoWithClassDto> problems) { // 5개만

        if (problems.size() != 5) {
            System.out.println("========================");
            System.out.println("IndexOutOfBoundsException");
            return -1;
        }

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


    // 분류 작업을 시작하면 문제 한 세트(5개) 제공
    public List<ProblemDtoWithClassDto> provideClassificationProblems(String userId, HttpServletRequest request, HttpServletResponse response) {

        String dataType = requestService.getDataTypeH(request);

        List<ProblemDtoWithClassDto> problemSet = combineProblems(dataType);

        if(problemSet.isEmpty() || problemSet.size() != 5) {
            System.out.println("========================");
            System.out.println("분류 문제 한 세트(5개)를 만들 수가 없음");
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

    // 라벨링 문제(2개) 가져오기
    private void labellingProblems(String dataType, List<ProblemEntity> selectedProblems) {

        if (projectRepository.countByWorkTypeAndDataType("labelling", dataType) > 1) {
            List<ProjectEntity> labellingProjects =
                    projectRepositoryImpl.labellingProjectSearch("labelling", dataType);

            for(ProjectEntity p : labellingProjects) {
                int projectId = p.getProjectId();

                Optional<ProblemEntity> labellingProblem = problemRepository.findFirstByProjectIdAndValidationStatus(projectId, "작업전");
                selectedProblems.add(labellingProblem.get());
            }

            System.out.println("========================");
            System.out.println("교차검증 문제 각각은 서로 다른 프로젝트에서 가져옴");
            return;
        }

        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByWorkTypeAndDataType("labelling", dataType);

        if (projectEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("라벨링 분류 프로젝트를 찾을 수가 없음");
            return;
        }

        int projectId = projectEntityWrapper.get().getProjectId();
        List<ProblemEntity> labellingProblems
                = problemRepositoryImpl.labellingProblem(projectId, "작업전", 2);
        selectedProblems.addAll(labellingProblems);

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


    // 분류 작업
    public boolean classificationWork(String userId, LinkedHashMap<String, Object> parameterMap,
                                      HttpServletRequest request, HttpServletResponse response) {

        int historyId = requestService.getHistoryIdH(request);

        if(parameterMap.size() != 5) {
            // 답이 제대로 안오면 labellingWorkHistory 삭제 추가 ***
            labellingWorkHistoryRepository.deleteByHistoryId(historyId);
            System.out.println("========================");
            System.out.println("문제의 답이 5개가 아님. 라벨링 작업 기록 삭제되었으니 작업 재시작 요망");
            response.setHeader("answer", "fail - 작업 다시 시작");
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

            int referenceId = problemEntityWrapper.get().getReferenceId();
            crossValidateProblem(referenceId);
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
