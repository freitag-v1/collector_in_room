package swcapstone.freitag.springsecurityjpa.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor

// SpringSecurity에게 어플리케이션이 어느 요청에 "인증이 필요한지 안한지"를 알려주기 위해서 커스텀 filter 생성
// 왜냐, SpringSecurity는 Servlet의 filter를 기반으로 동작하므로
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/", "/signup", "/login").permitAll()
                .antMatchers("/hello", "/success", "/failure").hasRole("ADMIN")
                .anyRequest().authenticated();   // 나머지 모든 요청에 대해서는 인증을 요구
    }
}