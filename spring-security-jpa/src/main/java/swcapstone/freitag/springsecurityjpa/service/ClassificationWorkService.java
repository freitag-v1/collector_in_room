package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.dto.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.BoundingBoxEntity;
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
    public List<ProblemDtoWithClassDto> provideClassificationProblems(String userId, HttpServletRequest request, HttpServletResponse response) {

        String dataType = "classification";
        List<ProblemDto> problemSet = combineProblems();

        // 클래스 정보도 함께 주기 위해서 ProblemDto -> ProblemDtoWithClassDto 변환
        List<ProblemDtoWithClassDto> problemSetWithClassNames = withClassDtos(problemSet);

        if(problemSet.isEmpty() || problemSet.size() != 5) {
            System.out.println("========================");
            System.out.println("분류 문제 한 세트(5개)를 만들 수가 없음");
            response.setHeader("problems", "fail");
            return null;
        }

        // 교차검증 문제가 바운딩박스 문제 기반으로 만들어졌는가?
        withBoundingBoxDtos(problemSetWithClassNames);

        int historyId = saveLabellingWorkHistory(userId, dataType, problemSet);
        response.setHeader("workHistory", String.valueOf(historyId));
        response.setHeader("problems", "success");
        return problemSetWithClassNames;
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

        List<ProblemEntity> userValidation = problemRepositoryImpl.validations("검증완료", 1);

        if(userValidation.isEmpty()) {
            System.out.println("========================");
            System.out.println("검증완료 문제를 찾을 수 없음");
            return;
        }

        int projectId = userValidation.get(0).getProjectId();
        int problemId = userValidation.get(0).getProblemId();
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

    // 분류 문제(2개) 가져오기
    private void labellingProblems(List<ProblemEntity> selectedProblems) {

        if (projectRepository.countByWorkTypeAndDataType("labelling", "classification") > 1) {
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
        selectedProblems.addAll(labellingProblems);

    }


    private List<ProblemDto> combineProblems() {
        List<ProblemEntity> selectedProblems = new ArrayList<>();

        // 50개 랜덤으로 뽑음 - 테스트는 5개만 뽑을거임
        // 10개 (테스트 1개) = userValidation(검증완료)
        userValidationProblems(selectedProblems);
        // 20개 (테스트 2개) = crossValidation(작업후)
        crossValidationProblems(selectedProblems);
        // 20개 (테스트 2개) = labellingProblems(작업전)
        labellingProblems(selectedProblems);

        List<ProblemDto> problemSet = ObjectMapperUtils.mapAll(selectedProblems, ProblemDto.class);
        return problemSet;
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


    @Override
    protected boolean saveAnswer(int problemId, String answer, String userId) {
        if(answer.contains("b")) {

            // 바운딩 박스 작업에 대한 교차검증이라면
            // answer의 형태가
            // boxId(공백)boxId..
            String boxIdList[] = answer.split("b");

            for (String b : boxIdList) {

                System.out.println("========================");
                System.out.println("boxId : " + b);

                if(b.equals(""))
                    break;

                int referenceBoxId = Integer.parseInt(b);
                Optional<BoundingBoxEntity> boundingBoxEntityWrapper = boundingBoxRepository.findByBoxId(referenceBoxId);

                if (boundingBoxEntityWrapper.isEmpty()) {
                    System.out.println("========================");
                    System.out.println("boundingBoxEntityWrapper.isEmpty()");
                    return false;
                }

                // 굳이 만들 필요 없을듯
                String className = boundingBoxEntityWrapper.get().getClassName();
                String coordinates = boundingBoxEntityWrapper.get().getCoordinates();

                int boxId = getBoxIdTurn();
                BoundingBoxDto boundingBoxDto = new BoundingBoxDto(boxId, problemId, className, coordinates);
                boundingBoxRepository.save(boundingBoxDto.toEntity());
            }

            answer = "boundingBox";
            // answer = boxId1 boxId2 이런 식으로 고치고
        }

        return super.saveAnswer(problemId, answer, userId);
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

        Optional<ProjectEntity> projectEntityWrapper
                = projectRepository.findByProjectId(projectId);

        if (projectEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("DB 에러 - 프로젝트 찾을 수 없음");
            return;
        }

        boolean isBoundingBox = false;

        if (projectEntityWrapper.get().getDataType().equals("boundingBox")) {
            isBoundingBox = true;
        }

        String finalAnswer = findFinalAnswer(isBoundingBox, answers, workers);

        if (finalAnswer == null) {
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

            // validatedData++
            Optional<ProjectEntity> targetProject = projectRepository.findByProjectId(projectId);

            targetProject.ifPresent(selectProject -> {
                int validatedData = selectProject.getValidatedData();
                validatedData += 1;
                selectProject.setValidatedData(validatedData);

                projectRepository.save(selectProject);
            });

            // 난이도 -> difficulty 게산

            System.out.println("========================");
            System.out.println("교차검증 성공!");
        }
    }

    // Voting!

    private static String findFinalAnswer(boolean isBoundingBox, String[] answers, String[] workers) {

        // 아기(~50), 유치원생(~60), 초등학생(~70), 중학생(~80) -> 0표
        // 고등학생(~85) -> 1표
        // 대학생(~90) -> 2표
        // 척척박사(~95) -> 3표
        // 신(~100) -> 4표

        // 각 사용자별로 정확도 계산
        // 12표 이상인지 확인
        // Voting


        // 맞은 사람 수랑 총 참여자 수를 여기서 알아낼 수 있음


        return null;    // finalAnswer
    }


}
