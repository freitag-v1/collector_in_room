package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import swcapstone.freitag.springsecurityjpa.domain.CustomUser;
import swcapstone.freitag.springsecurityjpa.domain.UserDto;
import swcapstone.freitag.springsecurityjpa.domain.UserEntity;
import swcapstone.freitag.springsecurityjpa.service.AuthenticationService;
import swcapstone.freitag.springsecurityjpa.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

// Client <-- dto --> Controller(Servlet)
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/api/")
    public String home() { return "home"; }

    @RequestMapping("/api/login")
    public String login(@Param("userId") String userId, @Param("userPassword") String userPassword) {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, userPassword);
        System.out.println(authToken.getPrincipal());
        Authentication authentication = authenticationService.authenticate(authToken);

        // userId 찍힘
        // System.out.println("authtoken.getName(): "+authToken.getName());
        // System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());

        if(authentication != null)
            return "redirect:/success";
        else
            return "redirect:/failure";

    }

    // 회원가입 form 만들기 귀찮아서 .. 필드 추가해야함!
    @RequestMapping("/api/signup")
    public String signUp(
            @Param("userId") String userId, @Param("userPassword") String userPassword, @Param("userName") String userName,
            @Param("userBank") int userBank, @Param("userAccount") String userAccount, @Param("userPhone") String userPhone,
            @Param("userEmail") String userEmail, @Param("userAffiliation") String userAffiliation) {

        UserDto userDto = new UserDto();

        userDto.setUserId(userId);
        userDto.setUserPassword(userPassword);
        userDto.setUserName(userName);
        userDto.setUserBank(userBank);
        userDto.setUserAccount(userAccount);
        userDto.setUserPhone(userPhone);
        userDto.setUserEmail(userEmail);
        userDto.setUserAffiliation(userAffiliation);
        userDto.setUserVisit(1);
        userDto.setTotalPoint(0);
        userDto.setPoint(0);

        if(userService.signUp(userDto))
            return "redirect:/success";
        else
            return "redirect:/failure";

    }

    // 마이페이지
    @RequestMapping("/api/mypage")
    // Model: 데이터만 저장
    public String mypage(@AuthenticationPrincipal CustomUser user) {
        return "userEmail: "+user.getUserEmail();
    }
/*
    @GetMapping("/readOne")
    public Optional readOne(Long id) {
        return userRepository.findById(id);
    }

    @GetMapping("/readAll")
    public List readAll() {
        return userRepository.findAll();
    }
 */
}
