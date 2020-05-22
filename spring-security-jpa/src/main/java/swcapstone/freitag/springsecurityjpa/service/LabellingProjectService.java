package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class LabellingProjectService extends ProjectService {

    public void createLabellingProblem(String userId, MultipartHttpServletRequest uploadRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");
        int totalData = labellingDataList.size();

        String bucketName = request.getHeader("bucketName");

        for(MultipartFile f : labellingDataList) {
            String fileName = f.getOriginalFilename();
            File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + userId+fileName);
            f.transferTo(destinationFile);

            String objectId = objectStorageApiClient.putObject(bucketName, destinationFile);

            if(objectId.isEmpty()) {
                response.setHeader("upload"+fileName, "fail");
                return;
            }

        }

        setTotalData(userId, totalData);
        setCost(userId, response);
        response.setHeader("upload", "success");
        return;
    }

    @Transactional
    protected void setTotalData(String userId, int totalData) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserIdAndStatus(userId, "없음");

        projectEntityWrapper.ifPresent(selectProject -> {
            selectProject.setTotalData(totalData);

            projectRepository.save(selectProject);
        });

        System.out.println("<totalData>");
        System.out.println(projectEntityWrapper.get().getTotalData());

    }

}
