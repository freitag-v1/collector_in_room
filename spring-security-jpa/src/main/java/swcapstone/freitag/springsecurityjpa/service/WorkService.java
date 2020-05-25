package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.AnswerDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.CollectionWorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.AnswerRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.CollectionWorkHistoryRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProblemRepository;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WorkService {

    @Autowired
    ObjectStorageApiClient objectStorageApiClient;
    @Autowired
    ProblemRepository problemRepository;
    @Autowired
    CollectionWorkHistoryRepository collectionWorkHistoryRepository;
    @Autowired
    AnswerRepository answerRepository;

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

    public List<ProblemDto> provideProblems(HttpServletRequest request, HttpServletResponse response) {

        int projectId = getProjectId(request);

        List<ProblemEntity> projectEntities = problemRepository.findAllByProjectIdAndValidationStatus(projectId, "작업전");

        // 10개 (테스트 1개) = userValidation(검증완료)
        // 20개 (테스트 2개) = crossValidation(작업후)
        // 20개 (테스트 2개) = labellingProblem(작업전)

        // 50개 랜덤으로 뽑음 - 테스트는 5개만 뽑을거임
        Collections.shuffle(projectEntities);
        List<ProblemEntity> selectLabellingProblems = projectEntities.subList(0, 5);    // 5개만

        List<ProblemDto> problems = ObjectMapperUtils.mapAll(selectLabellingProblems, ProblemDto.class);

        if(problems.isEmpty()) {
            response.setHeader("problems", "fail");
            return null;
        }

        response.setHeader("problems", "success");
        return problems;
    }

    @Transactional
    public boolean labellingWork(String userId, HttpServletRequest request, HttpServletResponse response) {

        Map<String, String[]> problemIdAnswerMap = request.getParameterMap(); // <problemId, answer들>
        int isFifty = problemIdAnswerMap.size();

        if (isFifty == 5) {

            for(Map.Entry<String, String[]> entry : problemIdAnswerMap.entrySet()) {
                String key = entry.getKey();
                int problemId = Integer.parseInt(key);
                String[] answers = entry.getValue();

                if(!saveAnswers(problemId, userId, answers)) {
                    response.setHeader("answer", "fail");
                    return false;
                }
            }

        }

        response.setHeader("answer", "success");
        return true;
    }

    @Transactional
    protected boolean saveAnswers(int problemId, String userId, String[] answers) {

        for(String answer : answers) {
            AnswerDto answerDto = new AnswerDto(problemId, userId, answer);

            if (answerRepository.save(answerDto.toEntity()) == null)
                return false;
        }
        return true;
    }

}
