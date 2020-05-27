package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.service.AuthorizationService;
import swcapstone.freitag.springsecurityjpa.service.LabellingProjectService;
import swcapstone.freitag.springsecurityjpa.service.ProjectService;
import swcapstone.freitag.springsecurityjpa.service.WorkService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class WorkController {

    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    ProjectService projectService;
    @Autowired
    LabellingProjectService labellingProjectService;
    @Autowired
    WorkService workService;
    @Autowired
    ObjectStorageApiClient objectStorageApiClient;

    // 수집 작업
    @RequestMapping(value = "/api/work/collection", method = RequestMethod.POST)
    public void collectionWork(MultipartHttpServletRequest uploadRequest,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            int projectId = workService.getProjectId(request);

            int limit = projectService.getLimit(projectId);

            if(workService.collectionWork(userId, limit, uploadRequest, request, response)) {

                projectService.setProgressData(projectId, uploadRequest);
            }
        }
    }

    // 라벨링 작업할 프로젝트의 문제 50개 주기 - 테스트용에서는 5개를 준다고 가정
    @RequestMapping(value = "/api/work/start")
    public List<ProblemDto> provideProblems(HttpServletRequest request, HttpServletResponse response) {

        if (authorizationService.isAuthorized((request))) {
            String userId = authorizationService.getUserId(request);
            return workService.provideProblems(userId, request, response);
        }

        return null;
    }

    // 라벨링 작업
    @RequestMapping(value = "/api/work/labelling", method = RequestMethod.POST)
    public void labellingWork(@RequestBody LinkedHashMap<String, Object> problemIdAnswerMap,
            HttpServletRequest request, HttpServletResponse response) {

        if (authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);

            if(workService.labellingWork(userId, problemIdAnswerMap, request, response)) {
                int projectId = workService.getProjectId(request);

                labellingProjectService.setProgressData(projectId);
            }
        }
    }


    // 다운로드?
    @RequestMapping(value = "/api/download")
    public OutputStream download(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String bucketName = request.getParameter("bucketName");
        String objectName = request.getParameter("objectName");

        if(objectStorageApiClient.objectExists(bucketName, objectName)) {
            OutputStream outputStream = objectStorageApiClient.getObject(bucketName, objectName);
            return outputStream;
        }

        return null;
    }
}
