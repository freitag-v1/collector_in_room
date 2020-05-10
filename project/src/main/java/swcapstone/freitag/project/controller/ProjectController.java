package swcapstone.freitag.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swcapstone.freitag.project.service.CollectionProjectService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
public class ProjectController {
    @Autowired
    CollectionProjectService collectionProjectService;

    @RequestMapping("/api/project/collection")
    public String createCollectionProject(HttpServletRequest request, HttpServletResponse response) {

        if(collectionProjectService.createProject(request, response)) {
            return "성공";
        }

        return "실패";
    }

    @RequestMapping(value = "/api/project/upload/example", method = RequestMethod.POST)
    public void uploadExampleData(@RequestParam("file") MultipartFile file) throws IOException {

        String fileName = file.getOriginalFilename();

        File destinationFile = new File("/Users/woneyhoney/Desktop/collector_in_room/files" + fileName);
        // MultipartFile.transferTo() : 요청 시점의 임시 파일을 로컬 파일 시스템에 영구적으로 복사하는 역할을 수행
        file.transferTo(destinationFile);

        System.out.println(fileName + " is uploaded in my macbook!");
    }
}
