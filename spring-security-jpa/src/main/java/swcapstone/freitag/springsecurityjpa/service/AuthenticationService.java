package swcapstone.freitag.springsecurityjpa.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import swcapstone.freitag.springsecurityjpa.utils.JwtProperties;
import swcapstone.freitag.springsecurityjpa.domain.dto.CustomUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
public class AuthenticationService implements AuthenticationProvider {
    // AuthenticationProvider는 UserDetailsService에서 DB에서 불러온 userPassword와
    // 사용자가 입력한 userPassword를 매칭하여 로그인 인증처리
    @Autowired
    UserService userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void login(String userId, String userPassword, HttpServletResponse response) throws IOException, ServletException {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, userPassword);
        Authentication authentication = authenticate(authToken);

        if(authentication != null) {
            System.out.println(authentication.getPrincipal()+" 님이 로그인하셨습니다.");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 토큰 생성
            successfulAuthentication(response, authentication);
            // System.out.println("SecurityContextHolder: "+SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            response.setHeader("login", "success");
        }
        else {
            response.setHeader("login", "fail");
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 유저가 입력한 정보를 userId, userPassword로 만든다.(즉, 로그인한 아이디, 비번 정보를 담는다)
        // UsernamePasswordAuthenticationToken - username, password를 쓰는 form기반 인증을 처리하는 필터
        // AuthenticationManager를 통한 인증 실행
        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken)authentication;
        // UserDetailsService에서 유저정보를 불러온다.
        CustomUser userInfo = (CustomUser) userService.loadUserByUsername(authToken.getName());

        if(userInfo == null) {
            throw new UsernameNotFoundException(authToken.getName());
        }

        if(!matchPassword(userInfo.getPassword(), authToken.getCredentials())) {
            throw new BadCredentialsException("아이디 혹은 패스워드를 잘못 입력");
        }

        // 즉 security의 세션들은 내부 메모리(SecurityContextHolder)에 쌓고 꺼내쓰는 것이다.
        return new UsernamePasswordAuthenticationToken(userInfo.getUsername(), userInfo.getPassword(), userInfo.getAuthorities());
    }

    private boolean matchPassword(String userPassword, Object credentials) {
        // Spring Security는 Authentication(principal: 아이디, credential: 비밀번호)
        // 방식 중 credential 기반으로 함
        return passwordEncoder.matches((String)credentials, userPassword);
    }


    // 앞에서 필터에서 보내준 Authentication 객체를 이 AuthenticationProvider가 인증 가능한 클래스인지 확인하는 메서드
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void successfulAuthentication(HttpServletResponse response, Authentication authResult)
        throws IOException, ServletException {

        String principal = (String) authResult.getPrincipal();

        // JWT 토큰 생성
        int oneDay = 24 * 3600 * 1000;
        String jwtToken = JWT.create()
                .withSubject(principal)   // userId
                .withExpiresAt(new Date(System.currentTimeMillis() + oneDay))
                .sign(Algorithm.HMAC512(JwtProperties.SECRET.getBytes()));

        // response에 JWT 토큰 추가
        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);
    }
}
