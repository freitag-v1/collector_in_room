package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swcapstone.freitag.springsecurityjpa.domain.UserDto;
import swcapstone.freitag.springsecurityjpa.service.AuthenticationService;
import swcapstone.freitag.springsecurityjpa.service.UserService;

// Client <-- dto --> Controller(Servlet)
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/")
    public String home() { return "home"; }

    @GetMapping("/success")
    public String success()
    {
        return "success";
    }

    @GetMapping("/failure")
    public String failure()
    {
        return "failure";
    }

    @RequestMapping("login")
    public String login(@Param("userId") String userId, @Param("userPassword") String userPassword) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, userPassword);
        Authentication authentication = authenticationService.authenticate(authToken);

        if(authentication != null)
            return "redirect:/success";
        else
            return "redirect:/failure";

    }

    // 회원가입 form 만들기 귀찮아서 ..
    @RequestMapping("/signup")
    public String signUp(@Param("userId") String userId, @Param("userPassword") String userPassword, @Param("userName") String userName) {
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        userDto.setUserPassword(userPassword);
        userDto.setUserName(userName);

        if(userService.signUp(userDto))
            return "redirect:/success";
        else
            return "redirect:/failure";

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
