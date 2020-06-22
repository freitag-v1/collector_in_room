package swcapstone.freitag.springsecurityjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpringSecurityJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityJpaApplication.class, args);
    }

}
