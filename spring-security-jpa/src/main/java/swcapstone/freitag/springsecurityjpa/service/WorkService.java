package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.CollectionWorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.CollectionWorkHistoryRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProblemRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class WorkService {

    @Autowired
    ObjectStorageApiClient objectStorageApiClient;
    @Autowired
    ProblemRepository problemRepository;
    @Autowired
    CollectionWorkHistoryRepository collectionWorkHistoryRepository;

    public boolean collectionWork(String userId, MultipartHttpServletRequest uploadRequest,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");
        int isFifty = labellingDataList.size();

        if (isFifty == 5/* 실제는 50이어야 하는데 테스트는 5개로 진행*/) {
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

                int problemId = saveObjectId(projectId, objectId);
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
        int projectId = Integer.parseInt(strProjectId);

        return projectId;
    }

    @Transactional
    protected int saveObjectId(int projectId, String objectId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findFirstByProjectIdAndObjectId(projectId, "없음");
        if(problemEntityWrapper.isEmpty()) {
            return -1;
        }

        problemEntityWrapper.ifPresent(selectProblem -> {
            selectProblem.setObjectId(objectId);
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

}
