package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.service.AuthorizationService;
import swcapstone.freitag.springsecurityjpa.service.ProjectService;
import swcapstone.freitag.springsecurityjpa.service.WorkService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class WorkController {

    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    ProjectService projectService;
    @Autowired
    WorkService workService;

    @RequestMapping(value = "/api/work/collection", method = RequestMethod.POST)
    public void collectionWork(MultipartHttpServletRequest uploadRequest,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);

            if(workService.collectionWork(userId, uploadRequest, request, response)) {
                int projectId = workService.getProjectId(request);
                projectService.setProgressData(projectId);
            }
        }
    }
}
