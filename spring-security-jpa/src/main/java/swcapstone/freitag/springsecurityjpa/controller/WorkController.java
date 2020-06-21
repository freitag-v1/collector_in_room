package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDtoWithClassDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.WorkHistoryDto;
import swcapstone.freitag.springsecurityjpa.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class WorkController {

    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    UserService userService;
    @Autowired
    ProjectService projectService;
    @Autowired
    WorkService workService;
    @Autowired
    CollectionWorkService collectionWorkService;
    @Autowired
    ClassificationWorkService classificationWorkService;
    @Autowired
    BoundingBoxWorkService boundingBoxWorkService;
    @Autowired
    RequestService requestService;

    // 수집 작업
    @RequestMapping(value = "/api/work/collection", method = RequestMethod.POST)
    public void collectionWork(MultipartHttpServletRequest uploadRequest,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            int projectId = requestService.getProjectIdH(request);

            int limit = projectService.getLimit(projectId);

            if(collectionWorkService.collectionWork(userId, limit, uploadRequest, request, response)) {
                response.setHeader("upload", "success");
            }
        }
    }

    // /api/work/~/start 했는데 작업자가 작업을 안한다?
    // 그러면 이 때 만들어진 사용자 검증 문제와 작업 기록을 지워야 되는 요청을 클라이언트에서 날려줘야함!
    @RequestMapping(value = "/api/work/cancel")
    public void deleteWork(HttpServletRequest request, HttpServletResponse response) {

    }

    // 라벨링 분류 작업 문제 50개 주기 - 테스트용에서는 5개를 준다고 가정
    @RequestMapping(value = "/api/work/classification/start")
    public List<ProblemDtoWithClassDto> provideClassificationProblems(HttpServletRequest request, HttpServletResponse response) {

        if (authorizationService.isAuthorized((request))) {
            String userId = authorizationService.getUserId(request);
            return classificationWorkService.provideClassificationProblems(userId, request, response);
        }

        return null;
    }

    // 라벨링 분류 작업
    @RequestMapping(value = "/api/work/classification", method = RequestMethod.POST)
    public void labellingClassificationWork(@RequestBody LinkedHashMap<String, Object> problemIdAnswerMap,
            HttpServletRequest request, HttpServletResponse response) {

        if (authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);

            if(classificationWorkService.classificationWork(userId, problemIdAnswerMap, request, response)) {
                response.setHeader("answer", "success");
            }

        }
    }

    // 라벨링 이미지 바운딩 박스 작업 문제 50개 주기 - 테스트용에서는 5개를 준다고 가정
    @RequestMapping(value = "/api/work/boundingbox/start")
    public List<ProblemDtoWithClassDto> provideBoundingBoxProblems(HttpServletRequest request, HttpServletResponse response) {

        if (authorizationService.isAuthorized((request))) {
            String userId = authorizationService.getUserId(request);
            return boundingBoxWorkService.provideBoundingBoxProblems(userId, request, response);
        }

        return null;
    }


    // 라벨링 이미지 바운딩 작업
    @RequestMapping(value = "/api/work/boundingbox", method = RequestMethod.POST)
    public void labellingBoundingBoxWork(@RequestBody LinkedHashMap<String, Object> problemIdAnswerMap,
                              HttpServletRequest request, HttpServletResponse response) {

        if (authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);

            if(boundingBoxWorkService.boundingBoxWork(userId, problemIdAnswerMap, request, response)) {
                response.setHeader("answer", "success");
            }

        }
    }


    // 본인이 작업한 목록 확인
    @RequestMapping(value = "/api/work/all")
    public List<WorkHistoryDto> getWorkList(HttpServletRequest request, HttpServletResponse response) {
        if (authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            return workService.getWorkList(userId, response);
        }

        response.setHeader("login", "fail");
        return null;
    }
}
