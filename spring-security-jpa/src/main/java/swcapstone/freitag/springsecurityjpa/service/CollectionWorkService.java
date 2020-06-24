package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.domain.dto.CollectionWorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class CollectionWorkService extends WorkService {

    // 작업자가 수집한 데이터를 Object Storage에 업로드!
    private String uploadData(MultipartFile file, String bucketName) throws Exception {
        String fileName = file.getOriginalFilename();
        // 수정 포인트
        File destinationFile = new File("/Users/sooyeon/Desktop/data/" + fileName);
        file.transferTo(destinationFile);

        return objectStorageApiClient.putObject(bucketName, destinationFile);
    }

    // Object Storage에 수집한 데이터를 업로드 성공하면, 작업전 인 문제를 하나 찾아서 objectName을 저장하고 작업후로 상태 변경
    @Transactional
    protected int saveObjectName(int projectId, String objectName) {
        // 해당 수집 작업의 프로젝트 아이디로 파일 업로드 되지 않은 문제(작업전)를 찾음
        Optional<ProblemEntity> problemEntityWrapper =
                problemRepository.findFirstByProjectIdAndValidationStatus(projectId, "작업전");

        if(!problemEntityWrapper.isPresent()) {
            System.out.println("========================");
            System.out.println("프로젝트 아이디로 작업전 문제를 찾을 수 없음");
            return -1;
        }

        problemEntityWrapper.ifPresent(selectProblem -> {
            // 작업자가 업로드한 파일의 objectName 저장
            selectProblem.setObjectName(objectName);
            // 수집 작업전 -> 작업후
            selectProblem.setValidationStatus("작업후");

            problemRepository.save(selectProblem);
        });

        return problemEntityWrapper.get().getProblemId();
    }


    // 작업자의 수집 작업 기록
    @Transactional
    protected void saveCollectionWorkHistory(String userId, int problemId) {

        CollectionWorkHistoryDto collectionWorkHistoryDto = new CollectionWorkHistoryDto(problemId, userId);
        collectionWorkHistoryRepository.save(collectionWorkHistoryDto.toEntity());

    }


    public boolean collectionWork(String userId, int limit, MultipartHttpServletRequest uploadRequest,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        String className = requestService.getClassNameP(request);

        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");
        int numberOfData = labellingDataList.size();

        if (limit < 1) {
            System.out.println("========================");
            System.out.println("이미 필요한 데이터를 모두 수집");
            return false;
        }

        if (0 < numberOfData && numberOfData <= limit) {

            int projectId = requestService.getProjectIdH(request);
            String bucketName = requestService.getBucketNameH(request);

            for(MultipartFile f : labellingDataList) {

                String objectName = uploadData(f, bucketName);

                if(objectName == null) {
                    System.out.println("========================");
                    System.out.println("Object Storage에 데이터 업로드 실패");
                    response.setHeader("upload", "fail to upload data to Object Storage");
                    return false;
                }

                // 해당 수집 작업의 프로젝트 아이디로 파일 업로드 되지 않은 문제(작업전)를 찾음
                // 작업자가 업로드한 파일의 objectName 저장
                // 수집 작업전 -> 작업후
                int problemId = saveObjectName(projectId, objectName);

                if(problemId != -1) {
                    if (saveAnswer(problemId, className, userId)) {
                        saveCollectionWorkHistory(userId, problemId);
                        // 이 문제의 등급 정하기
                        updateLevel(userId, problemId);
                        // 교차검증 문제 만들기
                        createCrossValidationProblem(problemId);
                        continue;
                    }
                } else {
                    response.setHeader("upload", "fail to find problems");
                    return false;
                }
            }

            projectService.setProgressData(projectId, numberOfData);
            return true;
        }

        response.setHeader("upload", "fail - number of data exceeds limit");
        return false;
    }
}
