package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import swcapstone.freitag.springsecurityjpa.domain.CustomUser;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthenticationService implements AuthenticationProvider {
    // AuthenticationProvider는 UserDetailsService에서 DB에서 불러온 userPassword와
    // 사용자가 입력한 userPassword를 매칭하여 로그인 인증처리
    @Autowired
    UserService userService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Spring Security 로그인 과정
    // 사용자가 요청한 서비스가 로그인이 필요 -> SpringSecurity는 SpringSecurityContext에서 Authentication이라는 객체를 찾는다.

    // 이때 Authentication 객체가 없으면 사용자에게 login 페이지를 보여주고
    // 사용자가 로그인 정보를 입력하고 로그인을 하면 사용자가 입력한 userID에 대한 User(UserDetails)를 읽어와서 사용자가 입력한 정보들과 비교

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 유저가 입력한 정보를 userId, userPassword로 만든다.(즉, 로그인한 아이디, 비번 정보를 담는다)
        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken)authentication;
        // UserDetailsService에서 유저정보를 불러온다.
        User userInfo = (User) userService.loadUserByUsername(authToken.getName());
        // CustomUser userInfo = (CustomUser) userService.loadUserByUsername(authToken.getName());
        System.out.println(userInfo.getUsername());

        if(userInfo == null) {
            throw new UsernameNotFoundException(authToken.getName());
        }

        if(!matchPassword(userInfo.getPassword(), authToken.getCredentials())) {
            throw new BadCredentialsException("아이디 혹은 패스워드를 잘못 입력");
        }

        // 로그인에 성공하면 Authentication 객체를 SpringSecurityContext에 담는다.
        return new UsernamePasswordAuthenticationToken(userInfo.getUsername(), userInfo.getPassword(), userInfo.getAuthorities());
    }

    private boolean matchPassword(String userPassword, Object credentials) {
        // Spring Security는 Authentication(principal: 아이디, credential: 비밀번호)
        // 방식 중 credential 기반으로 함
        System.out.println("userPassword: "+userPassword);
        System.out.println("credentials: "+credentials);

        return passwordEncoder.matches((String)credentials, userPassword);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
