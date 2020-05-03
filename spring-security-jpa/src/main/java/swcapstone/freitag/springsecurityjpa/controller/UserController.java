package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import swcapstone.freitag.springsecurityjpa.domain.CustomUser;
import swcapstone.freitag.springsecurityjpa.domain.UserDto;
import swcapstone.freitag.springsecurityjpa.service.AuthenticationService;
import swcapstone.freitag.springsecurityjpa.service.MyPageService;
import swcapstone.freitag.springsecurityjpa.service.UserService;

// 현재 사용자의 정보를 가지고 있는 Principal을 가져오려면?
// Authentication에서 Principal을 가져올 수 있고 Authentication은 SecurityContext에서,
// SecurityContext는 SecurityContextHolder를 통해 가져올 수 있다.

// Client <-- dto --> Controller(Servlet)
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private MyPageService myPageService;

    @RequestMapping("/api/login")
    public Authentication login(@Param("userId") String userId, @Param("userPassword") String userPassword) {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, userPassword);
        System.out.println(authToken.getPrincipal());
        Authentication authentication = authenticationService.authenticate(authToken);

        // userId 찍힘
        // System.out.println("authtoken.getName(): "+authToken.getName());
        // System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());

        if(authentication != null) {
            System.out.println(authentication.getPrincipal()+" 님이 로그인하셨습니다.");
            return authentication;
        }
        else {
            System.out.println("회원이 아닙니다.");
            return null;
        }

    }


    // 회원가입
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

    // 마이페이지 조회 (Read Only)
    @RequestMapping("/api/mypage")
    // @AuthenticationPrincipal: 컨트롤러단에서 세션의 정보들에 접근하고 싶을 때 파라미터에 선언
    // 이거 안쓰고 확인하려면 (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 이런식으로 써야한다.
    public String mypage(@AuthenticationPrincipal CustomUser user) {
        if(user == null) {
            return "redirect:/login";
        }
        // model.addAttribute(user);   // 뷰에 전달되는 모델 데이터
        return "로그인한 사용자 마이페이지 화면";
    }

    // 마이페이지 수정 - 비밀번호 한번 더 치라고 요구하는게 일반적일거 같음?
    @RequestMapping(value = "/api/mypage/update", method = RequestMethod.PUT)
    public String mypageUpdate(@AuthenticationPrincipal CustomUser user, UserDto userDto) {
        if(user == null) {
            return "redirect:/login";
        }
        myPageService.updateUserInfo(userDto);
        return "마이페이지 수정하고 반영된 마이페이지 화면";
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
