package swcapstone.freitag.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import swcapstone.freitag.project.service.CollectionProjectService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @RequestMapping(value = "/api/project/example", method = RequestMethod.PUT)
    public String uploadExampleData(HttpServletRequest request, HttpServletResponse response) {
        
    }
}
