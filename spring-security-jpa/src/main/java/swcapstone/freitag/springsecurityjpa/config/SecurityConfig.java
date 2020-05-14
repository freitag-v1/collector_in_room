package swcapstone.freitag.springsecurityjpa.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import swcapstone.freitag.springsecurityjpa.service.AuthenticationService;
import swcapstone.freitag.springsecurityjpa.service.AuthorizationService;
import swcapstone.freitag.springsecurityjpa.service.UserService;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
// SpringSecurity에게 어플리케이션이 어느 요청에 "인증이 필요한지 안한지"를 알려주기 위해서 커스텀 filter 생성
// 왜냐, SpringSecurity는 Servlet의 filter를 기반으로 동작하므로
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;
    @Autowired
    AuthenticationService authProvider;

    // Authentication을 보유하고 있는 SecurityContext는 인증 후에 기본적으로는 HttpSession에 저장
    // 다음에 같은 세션으로 접근이 되면 HttpSession에 저장되어 있는 SecurityContext가 얻어 지고 SecurityContextHolder에 저장
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                // csrf랑 session status 필요 없음
                // 왜냐하면 JWT 쓸거니까~
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // jwt 추가 (Authorization Service)
                // .addFilter(new AuthorizationService(authenticationManager()))
                .authorizeRequests()
                // access rule 설정
                .antMatchers( "/api/**").permitAll()
                .antMatchers( "/api/admin").hasRole("ADMIN");
                //.anyRequest().authenticated();

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // AuthenticationService(AuthenticationProvider 구현체) -> ProviderManager가 알 수 있게 등록
        // ProviderManager -> AuthenticationManager의 구현체로 스프링에서 인증을 담당
        // 그러나 직접 인증을 담당하는 것이 아니라, 멤버 변수로 가지고 있는 AuthenticationProvider들에게 인증을 위임
        // 즉, AuthenticationService가 authenticate() 메소드를 통해 인증에 성공하면, ProviderManager가 알려주는 느낌
        auth.authenticationProvider(authProvider);
        auth.userDetailsService(userService);
        auth.inMemoryAuthentication().passwordEncoder(getPasswordEncoder());
    }

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}