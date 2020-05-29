package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.CollectionWorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.LabellingWorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.LabellingWorkHistoryEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
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
    ProblemRepository problemRepository;
    @Autowired
    CollectionWorkHistoryRepository collectionWorkHistoryRepository;
    @Autowired
    LabellingWorkHistoryRepository labellingWorkHistoryRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    AnswerRepository answerRepository;

    private int labellingWorkHistoryIdTurn;

    private int getLabellingWorkHistoryIdTurn() {
        int count = (int) labellingWorkHistoryRepository.count();
        return ++count;
    }

    @Transactional
    protected void createCrossValidationProblem(int projectId, int problemId) {

        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        // 검증 대상 문제의 파일을 담은 bucketName
        String bucketName = problemEntityWrapper.get().getBucketName();
        // 검증 대상 문제의 파일 objectName
        String objectName = problemEntityWrapper.get().getObjectName();
        // 검증 대상 문제의 className
        String finalAnswer = problemEntityWrapper.get().getFinalAnswer();

        // 수집 프로젝트 -> objectName과 className 매칭이 제대로 되는가?
        if (projectService.isCollection(projectId)) {

            // 한 문제에 대한 교차검증 문제 2개만 만든다고 가정
            for(int i = 0; i < 2; i++) {
                int cvProblemId = projectService.getProblemIdTurn();
                // problemId, projectId, referenceId, bucketName, objectName, finalAnswer, validationStatus, userId
                ProblemDto problemDto = new ProblemDto(cvProblemId, projectId, problemId,
                        bucketName, objectName, finalAnswer, "교차검증전", null);
                problemRepository.save(problemDto.toEntity());
            }

            return;
        }

        // 한 문제에 대한 교차검증 문제 2개만 만든다고 가정
        for(int i = 0; i < 2; i++) {
            int cvProblemId = projectService.getProblemIdTurn();
            // problemId, projectId, referenceId, bucketName, objectName, finalAnswer, validationStatus, userId
            ProblemDto problemDto = new ProblemDto(cvProblemId, projectId, problemId,
                    bucketName, objectName, finalAnswer, "교차검증전", null);
            problemRepository.save(problemDto.toEntity());
        }

        return;
    }

    public boolean collectionWork(String userId, int limit, MultipartHttpServletRequest uploadRequest,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {

        String className = getClassName(request);

        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");
        int numberOfData = labellingDataList.size();

        if (limit < 1) {
            response.setHeader("upload", "fail - 필요한 데이터 수집 완료");
            return false;
        }

        int projectId = getProjectId(request);
        String bucketName = getBucketName(request);

        if (0 < numberOfData && numberOfData <= limit) {

            for(MultipartFile f : labellingDataList) {

                String fileName = f.getOriginalFilename();
                File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + userId+fileName);
                f.transferTo(destinationFile);

                String objectName = objectStorageApiClient.putObject(bucketName, destinationFile);

                if(objectName == null) {
                    response.setHeader("upload"+fileName, "fail");
                    return false;
                }

                int problemId = saveObjectName(projectId, objectName);
                if(problemId != -1) {
                    saveFinalAnswer(problemId, className);
                    saveUserId(problemId, userId);
                    saveCollectionWorkHistory(userId, problemId);
                    // 교차검증 문제 만들기
                    createCrossValidationProblem(projectId, problemId);
                    continue;
                } else {
                    response.setHeader("upload", "fail - problem_table 저장 공간 없음");
                    return false;
                }
            }
            response.setHeader("upload", "success");
            return true;
        }

        response.setHeader("upload", "fail - 업로드 개수 불일치");
        return false;
    }

    public int getProjectId(HttpServletRequest request) {
        String strProjectId = request.getHeader("projectId");
        return Integer.parseInt(strProjectId);
    }

    protected String getClassName(HttpServletRequest request) {
        String className = request.getParameter("className");
        return className;
    }

    protected String getBucketName(HttpServletRequest request) {
        String bucketName = request.getHeader("bucketName");
        return bucketName;
    }

    protected int getHistoryId(HttpServletRequest request) {
        String strHistoryId = request.getHeader("historyId");
        return Integer.parseInt(strHistoryId);
    }

    @Transactional
    protected int saveObjectName(int projectId, String objectName) {
        // 해당 수집 작업의 프로젝트 아이디로 파일 업로드 되지 않은 문제(작업전)를 찾음
        Optional<ProblemEntity> problemEntityWrapper =
                problemRepository.findFirstByProjectIdAndValidationStatus(projectId, "작업전");

        if(!problemEntityWrapper.isPresent()) {
            return -1;
        }

        // 수집 작업
        problemEntityWrapper.ifPresent(selectProblem -> {
            // 작업자가 업로드한 파일의 objectName 저장
            selectProblem.setObjectName(objectName);
            // 수집 작업전 -> 작업후
            selectProblem.setValidationStatus("작업후");

            problemRepository.save(selectProblem);
        });

        return problemEntityWrapper.get().getProblemId();
    }

    @Transactional
    protected void saveFinalAnswer(int problemId, String className) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if(!problemEntityWrapper.isPresent()) {
            return;
        }

        // 수집 작업
        problemEntityWrapper.ifPresent(selectProblem -> {

            // 작업자가 선택한 className을 finalAnswer에 저장
            selectProblem.setFinalAnswer(className);
            problemRepository.save(selectProblem);
        });
    }

    @Transactional
    protected void saveUserId(int problemId, String userId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if(!problemEntityWrapper.isPresent()) {
            return;
        }

        problemEntityWrapper.ifPresent(selectProblem -> {

            selectProblem.setUserId(userId);
            problemRepository.save(selectProblem);
        });
    }

    @Transactional
    protected void saveCollectionWorkHistory(String userId, int problemId) {

        CollectionWorkHistoryDto collectionWorkHistoryDto = new CollectionWorkHistoryDto(problemId, userId);
        collectionWorkHistoryRepository.save(collectionWorkHistoryDto.toEntity());
    }


    // 여기부터 라벨링 작업 관련
    protected String getDataType(HttpServletRequest request) {
        String dataType = request.getHeader("dataType");    // boundingBox or classification
        return dataType;
    }

    public List<ProblemDto> provideClassificationProblems(String userId, HttpServletRequest request, HttpServletResponse response) {

        String dataType = getDataType(request);

        List<ProblemDto> problems = combineProblems(dataType);

        if(problems.isEmpty()) {
            response.setHeader("problems", "fail");
            return null;
        }

        int historyId = saveLabellingWorkHistory(userId, dataType, problems);
        response.setHeader("workHistory", String.valueOf(historyId));
        response.setHeader("problems", "success");
        return problems;
    }

    @Transactional
    protected int createUserValidationProblem(int projectId, int problemId) {

        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        String bucketName = problemEntityWrapper.get().getBucketName();
        String objectName = problemEntityWrapper.get().getObjectName();
        String finalAnswer = problemEntityWrapper.get().getFinalAnswer();

        int uvProblemId = projectService.getProblemIdTurn();
        // problemId, projectId, referenceId, bucketName, objectName, finalAnswer, validationStatus, userId
        ProblemDto problemDto = new ProblemDto(uvProblemId, projectId, problemId,
                bucketName, objectName, finalAnswer, "사용자검증전", null);
        problemRepository.save(problemDto.toEntity());

        return uvProblemId;
    }

    private void userValidationProblems(List<ProblemEntity> selectedProblems) {
        // 10개 (테스트 1개) = userValidation(검증완료)
        List<ProblemEntity> userValidation = problemRepository
                .findAllByValidationStatus("검증완료");
        // 랜덤으로 섞어
        Collections.shuffle(userValidation);
        // 그 중 1개만 선택 (원래는 10개)
        List<ProblemEntity> selectedUserValidation = userValidation.subList(0, 1);
        // 새롭게 만들어지는 userValidation 문제들을 담을 리스트
        List<ProblemEntity> userValidationProblems = new ArrayList<>();
        // userValidation 문제 만들자 ..
        for(ProblemEntity p : selectedUserValidation) {
            int projectId = p.getProjectId();
            int problemId = p.getProblemId();
            // 검증완료된 문제로 또다시 userValidation 문제를 만들어
            int uvProblemId = createUserValidationProblem(projectId, problemId);
            // 만들어진 userValidation 문제의 problemId로 찾아
            Optional<ProblemEntity> userValidationProblem = problemRepository.findByProblemId(uvProblemId);
            // 리스트에 담자..
            userValidationProblems.add(userValidationProblem.get());
        }
        selectedProblems.addAll(userValidationProblems);
    }

    private void crossValidationProblems(List<ProblemEntity> selectedProblems) {
        // 20개 (테스트 2개) = crossValidation
        List<ProblemEntity> crossValidation = problemRepository.findAllByValidationStatus("교차검증전");
        // 랜덤으로 섞어
        Collections.shuffle(crossValidation);
        // 그 중 2개만 선택 (원래는 20개)
        List<ProblemEntity> selectedCrossValidation = crossValidation.subList(0, 2);
        // 새롭게 만들어지는 crossValidation 문제들을 담을 리스트
        List<ProblemEntity> crossValidationProblems = new ArrayList<>();
        // crossValidation 문제 가져오자 ...
        for(ProblemEntity p : selectedCrossValidation) {
            // 리스트에 담자..
            crossValidationProblems.add(p);
        }
        selectedProblems.addAll(crossValidationProblems);
    }

    private void labellingProblems(String dataType, List<ProblemEntity> selectedProblems) {
        // 20개 (테스트 2개) = labellingProblems(작업전)
        List<ProjectEntity> classificationProjects = projectRepository.findAllByDataType(dataType);
        Collections.shuffle(classificationProjects);
        List<ProjectEntity> selectedClassficationProjects = classificationProjects.subList(0, 2);

        for(ProjectEntity p : selectedClassficationProjects) {
            int projectId = p.getProjectId();

            Optional<ProblemEntity> labellingProblem = problemRepository.findFirstByProjectIdAndValidationStatus(projectId, "작업전");
            selectedProblems.add(labellingProblem.get());
        }
    }

    private List<ProblemDto> combineProblems(String dataType) {
        List<ProblemEntity> selectedProblems = new ArrayList<>();

        // 50개 랜덤으로 뽑음 - 테스트는 5개만 뽑을거임
        // 10개 (테스트 1개) = userValidation(검증완료)
        userValidationProblems(selectedProblems);
        // 20개 (테스트 2개) = crossValidation(작업후)
        crossValidationProblems(selectedProblems);
        // 20개 (테스트 2개) = labellingProblems(작업전)
        labellingProblems(dataType, selectedProblems);

        return ObjectMapperUtils.mapAll(selectedProblems, ProblemDto.class);
    }


    public void labellingWork(String userId, LinkedHashMap<String, Object> parameterMap,
                                 HttpServletRequest request, HttpServletResponse response) {

        String strHistoryId = request.getHeader("historyId");
        int historyId = Integer.parseInt(strHistoryId);

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

        if (problemEntityWrapper.get() == null)
            return false;

        problemEntityWrapper.ifPresent(selectProblem -> {
            selectProblem.setFinalAnswer(answer);
            selectProblem.setUserId(userId);

            problemRepository.save(selectProblem);
        });

        return true;
    }


    @Transactional
    protected int saveLabellingWorkHistory(String userId, String dataType, List<ProblemDto> problems) { // 5개만

        labellingWorkHistoryIdTurn = getLabellingWorkHistoryIdTurn();
        int historyId = this.labellingWorkHistoryIdTurn;

        int[] userValidationList = new int[1];      // new int[10];
        int[] crossValidationList = new int[2];     // new int[20];
        int[] labellingProblemList = new int[2];   // new int[20];

        for (int i = 0; i < 5; i++) {
            if (i < 1) {
                userValidationList[i] = problems.get(i).getProblemId();
                continue;
            } else if (i < 3) {
                crossValidationList[i - 1] = problems.get(i).getProblemId();
            } else {
                labellingProblemList[i - 3] = problems.get(i).getProblemId();
            }
        }

/*
        // 50개 기준

        int i = 0;

        int[] userValidationList = new int[10];
        while (i < 10) {
            userValidationList[i] = problems.get(i).getProblemId();
            i++;
        }

        int[] crossValidationList = new int[20];
        while (i < 30) {
            crossValidationList[i - 10] = problems.get(i).getProblemId();
            i++;
        }

        int[] labellingProblemList = new int[20];

        while (i < 50) {
            labellingProblemList[i - 30] = problems.get(i).getProblemId();
            i++;
        }



        LabellingWorkHistoryDto labellingWorkHistoryDto = new LabellingWorkHistoryDto(historyId, userId, dataType
                , userValidationList[0], userValidationList[1], userValidationList[2], userValidationList[3], userValidationList[4]
                , userValidationList[5], userValidationList[6], userValidationList[7], userValidationList[8], userValidationList[9]
                , crossValidationList[0], crossValidationList[1], crossValidationList[2], crossValidationList[3], crossValidationList[4]
                , crossValidationList[5], crossValidationList[6], crossValidationList[7], crossValidationList[8], crossValidationList[9]
                , crossValidationList[10], crossValidationList[11], crossValidationList[12], crossValidationList[13], crossValidationList[14]
                , crossValidationList[15], crossValidationList[16], crossValidationList[17], crossValidationList[18], crossValidationList[19]
                , labellingProblemList[0], labellingProblemList[1], labellingProblemList[2], labellingProblemList[3], labellingProblemList[4]
                , labellingProblemList[5], labellingProblemList[6], labellingProblemList[7], labellingProblemList[8], labellingProblemList[9]
                , labellingProblemList[10], labellingProblemList[11], labellingProblemList[12], labellingProblemList[13], labellingProblemList[14]
                , labellingProblemList[15], labellingProblemList[16], labellingProblemList[17], labellingProblemList[18], labellingProblemList[19]);
*/

        LabellingWorkHistoryDto labellingWorkHistoryDto = new LabellingWorkHistoryDto(historyId, userId, dataType
                , userValidationList[0], -1, -1, -1, -1
                , -1, -1, -1, -1, -1
                , crossValidationList[0], crossValidationList[1], -1, -1, -1
                , -1, -1, -1, -1, -1
                , -1, -1, -1, -1, -1
                , -1, -1, -1, -1, -1
                , labellingProblemList[0], labellingProblemList[1], -1, -1, -1
                , -1, -1, -1, -1, -1
                , -1, -1, -1, -1, -1
                , -1, -1, -1, -1, -1);

        if(labellingWorkHistoryRepository.save(labellingWorkHistoryDto.toEntity()) == null)
            return -1;

        return historyId;
    }

}
