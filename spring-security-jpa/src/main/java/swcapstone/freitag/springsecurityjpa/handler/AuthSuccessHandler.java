package swcapstone.freitag.springsecurityjpa.handler;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import swcapstone.freitag.springsecurityjpa.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class AuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static int TIME = 60 * 60 * 3; // 3시간

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        // 로그인 성공했을 떄 부가 작업 작성
        // 쿠키에 인증 토큰을 넣어줌
        super.onAuthenticationSuccess(request, response, authentication);
        System.out.println(authentication.getPrincipal()+"님 로그인 하셨습니다.");
        request.getSession().setMaxInactiveInterval(TIME);  // 3시간 후 타임아웃
        // userService.updateVisit((String) authentication.getPrincipal());    // 방문일 업데이트!

    }
}
