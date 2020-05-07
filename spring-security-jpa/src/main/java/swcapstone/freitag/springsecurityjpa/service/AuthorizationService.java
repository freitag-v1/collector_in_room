package swcapstone.freitag.springsecurityjpa.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import swcapstone.freitag.springsecurityjpa.JwtProperties;
import swcapstone.freitag.springsecurityjpa.domain.CustomUser;
import swcapstone.freitag.springsecurityjpa.domain.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.UserRepository;
import swcapstone.freitag.springsecurityjpa.domain.UserRole;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Authorization은 앞서 Authentication에서 획득한 JWT Token을 가지고 request를 요청할때 수행

// BasicAuthenticationFilter: HTTP request (for user authentication) 를 받고,
// userId와 userPassword를 뽑아냄 (from http request)
@Service
public class AuthorizationService extends BasicAuthenticationFilter {

    @Autowired
    UserService userService;

    public AuthorizationService(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    public boolean isAuthorized(HttpServletRequest request) {
        if(getUsernamePasswordAuthentication(request) != null)
            return true;

        return false;
    }

    // 모든 요청에 대해 Authorization 확인!
    // doFilterInternal 메서드는 authorization 이 포함된 request에 대한 endpoint
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        // JWT 토큰이 위치한 Authorization header를 읽어옴
        String header = request.getHeader(JwtProperties.HEADER_STRING);

        // header가 BEARER를 포함하지 않거나 null일 경우에는 종료
        if(header == null || !header.startsWith(JwtProperties.TOKEN_PREFIX)){
            chain.doFilter(request, response);
            return;
        }

        // header가 존재한다면, DB에서 user principal을 가져와서 Authorization 수행!
        Authentication authentication = getUsernamePasswordAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // filter 실행 계속
        chain.doFilter(request, response);
    }

    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request) {
        String token = request.getHeader(JwtProperties.HEADER_STRING);

        if(token != null) {
            // 토큰을 파싱해서 Decode!
            String userId = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET.getBytes()))
                    .build()
                    .verify(token.replace(JwtProperties.TOKEN_PREFIX, ""))
                    .getSubject();

            System.out.println("-----------------------------------"+userId+"-----------------------------------");

            // 토큰 subject에서 userId를 찾았다면 DB에서 정보를 확인!
            if (userId != null) {

                CustomUser customUser = (CustomUser) userService.loadUserByUsername(userId);
                User user = customUser.getUser();

                // 찾았다면 UserDetails를 통해 username, pass, authorities로 인증 토큰 생성!
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getUsername(), "null", user.getAuthorities());
                return authToken;

            }

            return null;
        }

        return null;
    }
}
