package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import swcapstone.freitag.springsecurityjpa.JwtProperties;
import swcapstone.freitag.springsecurityjpa.domain.CustomUser;
import swcapstone.freitag.springsecurityjpa.domain.UserDto;
import swcapstone.freitag.springsecurityjpa.service.AuthenticationService;
import swcapstone.freitag.springsecurityjpa.service.AuthorizationService;
import swcapstone.freitag.springsecurityjpa.service.MyPageService;
import swcapstone.freitag.springsecurityjpa.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 현재 사용자의 정보를 가지고 있는 Principal을 가져오려면?
// Authentication에서 Principal을 가져올 수 있고 Authentication은 SecurityContext에서,
// SecurityContext는 SecurityContextHolder를 통해 가져올 수 있다.

// Client <-- dto --> Controller(Servlet)
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


    @RequestMapping("/api/login")
    public Authentication login(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String userId = request.getParameter("userId");
        String userPassword = request.getParameter("userPassword");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, userPassword);
        Authentication authentication = authenticationService.authenticate(authToken);

        // userId 찍힘
        // System.out.println("authtoken.getName(): "+authToken.getName());
        // System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());

        if(authentication != null) {
            System.out.println(authentication.getPrincipal()+" 님이 로그인하셨습니다.");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 토큰 생성
            authenticationService.successfulAuthentication(response, authentication);

            System.out.println("SecurityContextHolder: "+SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            // System.out.println(response.getHeader(JwtProperties.HEADER_STRING));

            return authentication;
        }
        else {
            System.out.println("회원이 아닙니다.");
            return null;
        }

    }

    // 회원가입
    @RequestMapping("/api/signup")
    public String signUp(HttpServletRequest request) {

        if(userService.signUp(request))
            return "success";
        else
            return "fail";

    }


    // 마이페이지 조회 (Read Only)
    @RequestMapping("/api/mypage")
    // @AuthenticationPrincipal: 컨트롤러단에서 세션의 정보들에 접근하고 싶을 때 파라미터에 선언
    // 이거 안쓰고 확인하려면 (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 이런식으로 써야한다.
    // @AuthenticationPrincipal CustomUser user
    public CustomUser mypage(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            CustomUser user = (CustomUser) userService.loadUserByUsername(request.getParameter("userId"));
            System.out.println(user.getUsername()+" 님의 마이페이지 입니다!");
            return user;
        }

        else {
            System.out.println("로그인부터 하세요.");
            return null;
        }
    }


    // 마이페이지 수정
    @RequestMapping(value = "/api/mypage/update", method = RequestMethod.PUT)
    public String mypageUpdate(HttpServletRequest request) {

        if(authorizationService.isAuthorized(request)) {
            myPageService.updateUserInfo(request);
            return "수정 완료";
        }
        return "수정 실패";
    }

}
