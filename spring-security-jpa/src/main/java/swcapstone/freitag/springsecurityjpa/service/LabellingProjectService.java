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

    public void uploadLabellingData(String userId, MultipartHttpServletRequest uploadRequest,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");
        int totalData = labellingDataList.size();

        String strProjectId = request.getParameter("projectId");
        int projectId = Integer.parseInt(strProjectId);
        String bucketName = request.getHeader("bucketName");

        response.setHeader("projectId", strProjectId);

        for(MultipartFile f : labellingDataList) {
            String fileName = f.getOriginalFilename();
            File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + userId+fileName);
            f.transferTo(destinationFile);

            String objectId = objectStorageApiClient.putObject(bucketName, destinationFile);

            if(objectId == null) {
                response.setHeader("upload"+fileName, "fail");
                return;
            }

        }

        setTotalData(projectId, totalData);
        setCost(projectId, response);
        response.setHeader("upload", "success");
        return;
    }

    @Transactional
    protected void setTotalData(int projectId, int totalData) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        projectEntityWrapper.ifPresent(selectProject -> {
            selectProject.setTotalData(totalData);

            projectRepository.save(selectProject);
        });

        // System.out.println("<totalData>");
        // System.out.println(projectEntityWrapper.get().getTotalData());

    }

    @Override
    public void createProblem(int projectId, HttpServletResponse response) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        if (projectEntityWrapper.get().getStatus().equals("진행중")) {
            String bucketName = projectEntityWrapper.get().getBucketName();
            String exampleContent = projectEntityWrapper.get().getExampleContent();

            List<String> objectIdList = objectStorageApiClient.listObjects(bucketName);

            for(String s : objectIdList) {

                if (s.equals(exampleContent))
                    continue;

                int problemId = ++problemIdTurn;

                ProblemDto problemDto = new ProblemDto(problemId, projectId, -1, s, "없음", "작업전");

                if (problemRepository.save(problemDto.toEntity()) == null) {
                    response.setHeader("createProblem"+problemDto.getProblemId(), "fail");
                    break;
                }
            }
        }

    }
}
