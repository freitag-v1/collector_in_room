package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import swcapstone.freitag.springsecurityjpa.api.OpenBankingClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;
import swcapstone.freitag.springsecurityjpa.service.AuthenticationService;
import swcapstone.freitag.springsecurityjpa.service.AuthorizationService;
import swcapstone.freitag.springsecurityjpa.service.MyPageService;
import swcapstone.freitag.springsecurityjpa.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

// 현재 사용자의 정보를 가지고 있는 Principal을 가져오려면?
// Authentication에서 Principal을 가져올 수 있고 Authentication은 SecurityContext에서,
// SecurityContext는 SecurityContextHolder를 통해 가져올 수 있다.

// Client <-- dto --> Controller(Servlet)
@CrossOrigin(origins = "*")
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private MyPageService myPageService;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String userId = request.getParameter("userId");
        String userPassword = request.getParameter("userPassword");

        authenticationService.login(userId, userPassword, response);
    }

    // 회원가입
    @RequestMapping("/api/signup")
    public void signUp(HttpServletRequest request, HttpServletResponse response) {

        if(userService.signUp(request, response)) {
            response.setHeader("signup", "success");
            return;
        }

        response.setHeader("signup", "fail");

    }


    // 마이페이지 조회 (Read Only)
    @RequestMapping("/api/mypage")
    public CustomUser mypage(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            CustomUser user = (CustomUser) userService.loadUserByUsername(userId);
            System.out.println(user.getUsername()+" 님의 마이페이지 입니다!");
            myPageService.getUpdateProhibitedUserInfo(userId, response);
            return user;
        }

        else {
            System.out.println("로그인부터 하세요.");
            return null;
        }
    }


    // 마이페이지 수정
    @RequestMapping(value = "/api/mypage/update", method = RequestMethod.PUT)
    public void mypageUpdate(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            myPageService.updateUserInfo(request, userId);
            response.setHeader("update", "success");
            return;
        }
        response.setHeader("update", "fail");
    }

    // 마이페이지 - 포인트 환전
    @RequestMapping(value = "/api/mypage/exchange", method = RequestMethod.PUT)
    public void exchangePoint(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            int amount = request.getIntHeader("amount");

            Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
            userEntityWrapper.ifPresent(selectUser -> {
                if(amount <= selectUser.getPoint()) {
                    if(OpenBankingClient.getInstance().deposit(selectUser.getUserOpenBankingAccessToken(), selectUser.getUserOpenBankingNum(), "테스트", amount)) {
                        selectUser.setPoint(selectUser.getPoint() - amount);
                    }
                }
            });
        }
    }

    // 누적 포인트별 랭킹 갱신 기능
    @RequestMapping(value = "/api/ranking/point")
    public List<TotalPointRankUserDto> top10RichUser(HttpServletResponse response) {
        return userService.rankingUpdateByTotalPoint(response);
    }

    // 정확도별 랭킹 갱신 기능
    @RequestMapping(value = "/api/ranking/accuracy")
    public List<AccuracyRankUserDto> top10SmartUser(HttpServletResponse response) {
        return userService.rankingUpdateByAccuracy(response);
    }
}
