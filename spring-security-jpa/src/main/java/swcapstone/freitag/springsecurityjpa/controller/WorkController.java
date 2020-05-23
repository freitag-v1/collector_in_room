package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.service.AuthorizationService;
import swcapstone.freitag.springsecurityjpa.service.ProjectService;
import swcapstone.freitag.springsecurityjpa.service.WorkService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class WorkController {

    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    ProjectService projectService;
    @Autowired
    WorkService workService;

    // 수집 작업
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

    // 라벨링 작업할 프로젝트의 문제 50개 주기 - 테스트용에서는 5개를 준다고 가정
    @RequestMapping(value = "/api/work/labelling")
    public List<ProblemDto> provideProblems(HttpServletRequest request, HttpServletResponse response) {

        if (authorizationService.isAuthorized((request))) {
            return workService.provideProblems(request, response);
        }

        return null;
    }
}
