package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.AnswerDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.CollectionWorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.LabellingWorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.AnswerRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.CollectionWorkHistoryRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.LabellingWorkHistoryRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProblemRepository;
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
    ProblemRepository problemRepository;
    @Autowired
    CollectionWorkHistoryRepository collectionWorkHistoryRepository;
    @Autowired
    LabellingWorkHistoryRepository labellingWorkHistoryRepository;
    @Autowired
    AnswerRepository answerRepository;

    private int labellingWorkHistoryIdTurn;

    private int getLabellingWorkHistoryIdTurn() {
        int count = (int) labellingWorkHistoryRepository.count();
        return ++count;
    }

    public boolean collectionWork(String userId, int limit, MultipartHttpServletRequest uploadRequest,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");
        int numberOfData = labellingDataList.size();

        if (limit < 1) {
            response.setHeader("upload", "fail - 필요한 데이터 수집 완료");
            return false;
        }

        if (0 < numberOfData && numberOfData <= limit) {
            int projectId = getProjectId(request);

            String bucketName = request.getHeader("bucketName");

            for(MultipartFile f : labellingDataList) {
                String fileName = f.getOriginalFilename();
                File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + userId+fileName);
                f.transferTo(destinationFile);

                String objectId = objectStorageApiClient.putObject(bucketName, destinationFile);

                if(objectId == null) {
                    response.setHeader("upload"+fileName, "fail");
                    return false;
                }

                int problemId = saveObjectName(projectId, objectId);
                if(problemId != -1) {
                    saveCollectionWorkHistory(userId, problemId, response);
                    continue;
                } else {
                    response.setHeader("upload", "fail - problem_table 저장 공간 없음");
                    return false;
                }
            }

            // project의 progress_data 업데이트

            response.setHeader("upload", "success");
            return true;
        }

        response.setHeader("upload", "fail - 업로드 개수 불일치");
        return false;
    }

    public int getProjectId(HttpServletRequest request) {
        String strProjectId = request.getParameter("projectId");

        return Integer.parseInt(strProjectId);
    }

    @Transactional
    protected int saveObjectName(int projectId, String objectName) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findFirstByProjectIdAndObjectName(projectId, "없음");
        if(problemEntityWrapper.isEmpty()) {
            return -1;
        }

        problemEntityWrapper.ifPresent(selectProblem -> {
            selectProblem.setObjectName(objectName);
            selectProblem.setValidationStatus("작업후");

            problemRepository.save(selectProblem);
        });

        return problemEntityWrapper.get().getProblemId();
    }

    @Transactional
    protected void saveCollectionWorkHistory(String userId, int problemId, HttpServletResponse response) {
        CollectionWorkHistoryDto collectionWorkHistoryDto = new CollectionWorkHistoryDto(problemId, userId);

        if(collectionWorkHistoryRepository.save(collectionWorkHistoryDto.toEntity()) == null) {
            response.setHeader("createHist", "fail");
        }
    }


    // 여기부터 라벨링 작업 관련
    private String getDataType(HttpServletRequest request) {
        String dataType = request.getHeader("dataType");    // boundingBox or classification
        return dataType;
    }

    public List<ProblemDto> provideProblems(String userId, HttpServletRequest request, HttpServletResponse response) {

        int projectId = getProjectId(request);
        String dataType = getDataType(request);

        List<ProblemEntity> selectLabellingProblems = new ArrayList<>();

        // 50개 랜덤으로 뽑음 - 테스트는 5개만 뽑을거임

        // 10개 (테스트 1개) = userValidation(검증완료)
        List<ProblemEntity> userValidation = problemRepository
                .findAllByProjectIdAndValidationStatus(projectId, "검증완료");
        Collections.shuffle(userValidation);
        List<ProblemEntity> selectedUserValidation = userValidation.subList(0, 1);
        selectLabellingProblems.addAll(selectedUserValidation);

        // 20개 (테스트 2개) = crossValidation(작업후)
        List<ProblemEntity> crossValidation = problemRepository
                .findAllByProjectIdAndValidationStatus(projectId, "작업후");
        Collections.shuffle(crossValidation);
        List<ProblemEntity> selectedCrossValidation = crossValidation.subList(0, 2);
        selectLabellingProblems.addAll(selectedCrossValidation);

        // 20개 (테스트 2개) = labellingProblems(작업전)
        List<ProblemEntity> labellingProblems = problemRepository
                .findAllByProjectIdAndValidationStatus(projectId, "작업전");
        Collections.shuffle(labellingProblems);
        List<ProblemEntity> selectedLabellingProblems = labellingProblems.subList(0, 2);
        selectLabellingProblems.addAll(selectedLabellingProblems);

        List<ProblemDto> problems = ObjectMapperUtils.mapAll(selectLabellingProblems, ProblemDto.class);

        if(problems.isEmpty()) {
            response.setHeader("problems", "fail");
            return null;
        }

        saveLabellingWorkHistory(userId, dataType, selectedUserValidation, selectedCrossValidation, selectLabellingProblems);
        response.setHeader("problems", "success");
        return problems;
    }


    public boolean labellingWork(String userId, LinkedHashMap<String, Object> parameterMap, HttpServletResponse response) {

        LinkedHashMap<String, String> problemIdAnswerMap = new LinkedHashMap<>();
        for(String problemId : parameterMap.keySet()) {

            problemIdAnswerMap.put(problemId, parameterMap.get(problemId).toString());
        }

        for(Map.Entry<String, String> entry : problemIdAnswerMap.entrySet()) {

            String strProblemId = entry.getKey();
            int problemId = Integer.parseInt(strProblemId);

            String answers = entry.getValue();

            System.out.println("======================");
            System.out.println("problemId : " + problemId);
            System.out.println("answers : " + answers);

            if(!saveAnswers(problemId, userId, answers)) {
                // 답이 제대로 저장이 안되면 labellingWorkHistory 삭제 추가 ***
                response.setHeader("answer", "fail");
                return false;
            }

            // 답이 제대로 저장이 되면 problem_table에서 해당 problem의 validation_status 작업전->작업후 변경
            updateValidationStatus(problemId);
        }

        response.setHeader("answer", "success");
        return true;
    }


    @Transactional
    protected void updateValidationStatus(int problemId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        problemEntityWrapper.ifPresent(selectProblem -> {
            selectProblem.setValidationStatus("작업후");
            problemRepository.save(selectProblem);
        });
    }


    @Transactional
    protected boolean saveAnswers(int problemId, String userId, String answer) {

        if (answer.contains("&")) {
            String[] answers = answer.split("&");

            for(int i = 0; i < answers.length; i++) {
                AnswerDto answerDto = new AnswerDto(problemId, userId, answers[i]);
                if (answerRepository.save(answerDto.toEntity()) == null)
                    return false;
            }

            return true;
        }

        AnswerDto answerDto = new AnswerDto(problemId, userId, answer);
        if (answerRepository.save(answerDto.toEntity()) == null)
            return false;

        return true;
    }



    @Transactional
    protected void saveLabellingWorkHistory(String userId, String dataType, List<ProblemEntity> userValidation,
                                            List<ProblemEntity> crossValidation, List<ProblemEntity> labellingProblems) {

        labellingWorkHistoryIdTurn = getLabellingWorkHistoryIdTurn();
        int historyId = this.labellingWorkHistoryIdTurn;

        int[] userValidationList = new int[10];
        for(int i = 0; i < 10; i++) {
            if (i < 1)
                userValidationList[i] = userValidation.get(i).getProblemId();
            else
                userValidationList[i] = -1;
        }

        int[] crossValidationList = new int[20];
        for(int i = 0; i < 20; i++) {
            if (i < 2)
                crossValidationList[i] = crossValidation.get(i).getProblemId();
            else
                crossValidationList[i] = -1;
        }

        int[] labellingProblemList = new int[20];
        for(int i = 0; i < 20; i++) {
            if (i < 2)
                labellingProblemList[i] = labellingProblems.get(i).getProblemId();
            else
                labellingProblemList[i] = -1;
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


        labellingWorkHistoryRepository.save(labellingWorkHistoryDto.toEntity());
    }

}
