package swcapstone.freitag.springsecurityjpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

// Client <-- dto --> Controller(Servlet)
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    // 클라이언트는 URL로 요청을 전송
    // 요청 URL을 어떤 메서드가 처리할지 여부를 결정하는 것이 “@RequestMapping“

    @RequestMapping("/success")
    public String success()
    {
        return "signup success";
    }

    @RequestMapping("/failure")
    public String failure()
    {
        return "signup failure";
    }

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

    @GetMapping("/readuser")
    public UserDetails readByUserId(String userId) {
        return userService.loadUserByUsername(userId);
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
