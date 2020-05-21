package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

@Service
public class LabellingProjectService extends ProjectService {

    public void createLabellingProblem(String userId, MultipartHttpServletRequest uploadRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<MultipartFile> labellingDataList = uploadRequest.getFiles("files");

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

        response.setHeader("upload", "success");
        return;
    }
}
