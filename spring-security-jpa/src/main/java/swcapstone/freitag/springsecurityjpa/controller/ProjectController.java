package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDtoWithClassDto;
import swcapstone.freitag.springsecurityjpa.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@RestController
public class ProjectController {

    @Autowired
    RequestService requestService;
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

            int num = (int) (Math.random() * 100) + 1;  // 1 ~ 100

            // 버킷 생성
            String bucketName = userId + num;
            if(objectStorageApiClient.putBucket(bucketName)) {
                // 버킷 생성되면 수집 프로젝트 생성에 필요한 사용자 입력 필드와 함께 디비 저장
                projectService.createProject(request, userId, bucketName, response);
            }
        }

    }

    @RequestMapping(value = "/api/project/class", method = RequestMethod.POST)
    public void createClass(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            projectService.createClass(request, response);
        }

    }

    @RequestMapping(value = "/api/project/upload/example", method = RequestMethod.POST)
    public void uploadExampleData(HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                  HttpServletResponse response) throws Exception {

        if(authorizationService.isAuthorized(request)) {
            projectService.uploadExampleContent(request, file, response);
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

            if(userService.accountPayment(userId, "프로젝트 생성", cost, response)) {

                projectService.setNextStatus(projectId, response);

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

            int projectId = requestService.getProjectIdP(request);
            int cost = projectService.getCost(projectId);

            if(userService.pointPayment(userId, cost, response)) {

                projectService.setNextStatus(projectId, response);

                if(projectService.isCollection(projectId)) {
                    projectService.createProblem(projectId, response);
                } else {
                    labellingProjectService.createProblem(projectId, response);
                }

            } else {
                // 포인트 없어서 결제 실패
                return;
            }

        }
    }

    // 본인이 의뢰한 프로젝트 목록 확인
    @RequestMapping(value = "/api/project/all")
    public List<ProjectDtoWithClassDto> getProjectList(HttpServletRequest request, HttpServletResponse response) {
        if(authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            return projectService.getProjectList(userId, response);
        }

        response.setHeader("login", "fail");
        return null;
    }

    // 프로젝트 종료
    @RequestMapping(value = "/api/project/terminate")
    public void terminateProject(HttpServletRequest request, HttpServletResponse response) {
        if(authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            int projectId = requestService.getProjectIdP(request);

            Integer finalCost = projectService.calculateFinalCost(userId, projectId, response);
            if(finalCost == null) {
                response.setHeader("project", "fail");
            } else {
                response.setHeader("project", "success");
                response.setHeader("finalCost", finalCost.toString());
            }
        }
    }

    // 프로젝트 종료
    @RequestMapping(value = "/api/project/terminate/account")
    public void terminateProjectByAccount(HttpServletRequest request, HttpServletResponse response) {
        if(authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            int projectId = requestService.getProjectIdP(request);

            Integer finalCost = projectService.calculateFinalCost(userId, projectId, response);
            if(finalCost == null) {
                response.setHeader("payment", "fail");
            } else {
                if(userService.accountPayment(userId, "프로젝트 종료", finalCost, response)) {
                    projectService.setNextStatus(projectId, response);
                    // 압축 시작
                }
            }
        }
    }

    // 프로젝트 종료
    @RequestMapping(value = "/api/project/terminate/point")
    public void terminateProjectByPoint(HttpServletRequest request, HttpServletResponse response) {
        if(authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            int projectId = requestService.getProjectIdP(request);

            Integer finalCost = projectService.calculateFinalCost(userId, projectId, response);
            if(finalCost == null) {
                response.setHeader("project", "fail");
            } else {
                if(userService.pointPayment(userId, finalCost, response)) {
                    projectService.setNextStatus(projectId, response);
                    // 압축 시작
                }
            }
        }
    }

    // 프로젝트 다운로드
    @RequestMapping(value = "/api/project/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> downloadProject(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            int projectId = requestService.getProjectIdP(request);

            File zipFile = projectService.downloadProject(userId, projectId, response);
            if(zipFile != null) {
                InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));
                return ResponseEntity.ok()
                        .contentLength(zipFile.length())
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(resource);
            }
        }
        return null;
    }
}
