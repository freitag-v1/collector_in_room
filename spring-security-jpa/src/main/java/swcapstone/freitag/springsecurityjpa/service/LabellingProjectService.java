package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class LabellingProjectService extends ProjectService {

    // 의뢰자
    public void uploadLabellingData(String userId, MultipartHttpServletRequest uploadRequest,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<MultipartFile> labellingDataList = getLabellingDataList(uploadRequest);
        int totalData = labellingDataList.size();

        String bucketName = requestService.getBucketNameH(request);
        int projectId = getProjectId(bucketName);

        for(MultipartFile f : labellingDataList) {
            String fileName = f.getOriginalFilename();
            File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + userId+fileName);
            f.transferTo(destinationFile);

            String objectName = objectStorageApiClient.putObject(bucketName, destinationFile);

            if(objectName == null) {
                response.setHeader("upload"+fileName, "fail");
                return;
            }

        }

        setTotalData(projectId, totalData);
        setCost(projectId, response);
        response.setHeader("upload", "success");
        return;
    }

    private List<MultipartFile> getLabellingDataList(MultipartHttpServletRequest uploadRequest) {
        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");
        return labellingDataList;
    }

    private int getProjectId(String bucketName) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByBucketName(bucketName);
        return projectEntityWrapper.get().getProjectId();
    }

    @Transactional
    protected void setTotalData(int projectId, int totalData) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        projectEntityWrapper.ifPresent(selectProject -> {
            selectProject.setTotalData(totalData);

            projectRepository.save(selectProject);
        });

    }

    @Override
    public void createProblem(int projectId, HttpServletResponse response) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        if (projectEntityWrapper.get().getStatus().equals("진행중")) {
            String bucketName = projectEntityWrapper.get().getBucketName();
            String exampleContent = projectEntityWrapper.get().getExampleContent();

            List<String> objectNameList = objectStorageApiClient.listObjects(bucketName);

            for(String s : objectNameList) {

                // 예시 데이터 제외하고 문제 만들어야 되므로
                if (s.equals(exampleContent))
                    continue;

                problemIdTurn = getProblemIdTurn();
                int problemId = this.problemIdTurn;

                ProblemDto problemDto = new ProblemDto(problemId, projectId, -1, bucketName, s
                        , null, null, "작업전", null);

                if (problemRepository.save(problemDto.toEntity()) == null) {
                    response.setHeader("createProblem"+problemDto.getProblemId(), "fail");
                    break;
                }
            }
        }

    }
}
