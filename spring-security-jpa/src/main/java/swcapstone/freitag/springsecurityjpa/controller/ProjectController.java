package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDtoWithClassDto;
import swcapstone.freitag.springsecurityjpa.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class ProjectController {

    @Autowired
    ProjectService projectService;
    @Autowired
    LabellingProjectService labellingProjectService;
    @Autowired
    ObjectStorageApiClient objectStorageApiClient;
    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    UserService userService;


    @RequestMapping(value = "/api/project/create", method = RequestMethod.POST)
    public void createProject(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            int howManyProjects = projectService.howManyProjects(userId) + 1;

            // 버킷 생성
            String bucketName = userId+howManyProjects;
            if(objectStorageApiClient.putBucket(bucketName)) {
                // 버킷 생성되면 수집 프로젝트 생성에 필요한 사용자 입력 필드와 함께 디비 저장
                projectService.createProject(request, userId, bucketName, response);
            }
        }

    }

    @RequestMapping(value = "/api/project/class", method = RequestMethod.POST)
    public void createClass(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            projectService.createClass(userId, request, response);
        }

    }

    @RequestMapping(value = "/api/project/upload/example", method = RequestMethod.POST)
    public void uploadExampleData(HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                  HttpServletResponse response) throws Exception {

        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);

            // 예시 데이터 object의 Etag를 exampleContent로 지정하고 cost 설정하고 헤더에 붙이기
            projectService.uploadExampleContent(userId, request, file, response);
        }
    }


    @RequestMapping(value = "/api/project/upload/labelling", method = RequestMethod.POST)
    public void uploadLabellingData(MultipartHttpServletRequest uploadRequest,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {

        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);

            labellingProjectService.uploadLabellingData(userId, uploadRequest, request, response);
        }

    }


    // 프로젝트 검색 결과 반환
    // workType, dataType, subject, difficulty
    @RequestMapping(value = "/api/project/list")
    public List<ProjectDtoWithClassDto> getCollectionSearchResults(HttpServletRequest request, HttpServletResponse response) {

        // 검색은 로그인 안해도 누구나 할 수 있음
        return projectService.getSearchResults(request, response);

    }


    // 오픈 뱅킹 결제
    // 결제 완료되면 status 없음 -> 진행중 변경할 것
    @RequestMapping(value = "/api/project/account/payment")
    public void payInAccount(HttpServletRequest request, HttpServletResponse response) {
        if(authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            String strProjectId = request.getParameter("projectId");
            int projectId = Integer.parseInt(strProjectId);

            int cost = projectService.getCost(projectId);

            if(userService.accountPayment(userId, cost, response)) {

                projectService.setStatus(projectId, response);

                if(projectService.isCollection(projectId)) {
                    projectService.createProblem(projectId, response);
                } else {
                    labellingProjectService.createProblem(projectId, response);
                }
            } else {
                return;
            }

        }
    }

    // 사용자 포인트로 결제
    // 결제 완료되면 status 없음 -> 진행중 변경할 것
    @RequestMapping(value = "/api/project/point/payment")
    public void payInPoints(HttpServletRequest request, HttpServletResponse response) {
        if(authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            String strProjectId = request.getParameter("projectId");
            int projectId = Integer.parseInt(strProjectId);

            int cost = projectService.getCost(projectId);

            if(userService.pointPayment(userId, cost, response)) {

                projectService.setStatus(projectId, response);

                if(projectService.isCollection(projectId)) {
                    projectService.createProblem(projectId, response);
                } else {
                    labellingProjectService.createProblem(projectId, response);
                }

            } else {
                return;
            }

        }
    }
}
