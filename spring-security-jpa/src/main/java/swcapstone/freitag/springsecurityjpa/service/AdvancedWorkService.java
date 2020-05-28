package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class AdvancedWorkService extends WorkService {
/*
    public boolean collectionAndLabelling(String userId, int limit, MultipartHttpServletRequest uploadRequest,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception {

        MultipartFile uploadFile = getFile(uploadRequest);
        int projectId = getProjectId(request);
        String bucketName = getBucketName(request);
        String className = getClassName(request);

        String fileName = uploadFile.getOriginalFilename();
        File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + userId+fileName);
        uploadFile.transferTo(destinationFile);

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
        } else {
            response.setHeader("upload", "fail - problem_table 저장 공간 없음");
            return false;
        }
    }

    private MultipartFile getFile(MultipartHttpServletRequest uploadRequest) {
        MultipartFile uploadFile = uploadRequest.getFile("file");
        return uploadFile;
    }
*/
}
