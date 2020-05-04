package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import swcapstone.freitag.springsecurityjpa.domain.AuthenticationToken;
import swcapstone.freitag.springsecurityjpa.domain.CustomUser;
import swcapstone.freitag.springsecurityjpa.domain.UserDto;
import swcapstone.freitag.springsecurityjpa.service.AuthenticationService;
import swcapstone.freitag.springsecurityjpa.service.MyPageService;
import swcapstone.freitag.springsecurityjpa.service.UserService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;

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

    @RequestMapping(value = "/api/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession();
        Object name = session.getAttribute("authenticationUser");
        System.out.println(name);
        session.invalidate();
        return "success";

    }

    @RequestMapping(value = "/api/login")
    public AuthenticationToken login(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

        String userId = request.getParameter("userId");
        String userPassword = request.getParameter("userPassword");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, userPassword);
        Authentication authentication = authenticationService.authenticate(authToken);
        System.out.println(authentication);
        // userId 찍힘
        // System.out.println("authtoken.getName(): "+authToken.getName());
        // System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());

        if(authentication != null) {
            System.out.println(authentication.getPrincipal()+" 님이 로그인하셨습니다.");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("SecurityContextHolder: "+SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
            session.setAttribute("authenticationUser",authentication.getPrincipal().toString());

            return new AuthenticationToken(authentication.getPrincipal().toString(),authentication.getAuthorities(),session.getId());
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
    // @AuthenticationPrincipal CustomUser user
    public String mypage(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(); //vue에서 요청한 request의 session에 접근
        Object userId = session.getAttribute("authenticationUser"); //request의 session에 접근해서 session attribute에 있는 userID를 가져옴
                //httpServletRequest.getParameter("userId")
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //context에 있는 정보랑 같은지 확인 session에 있는 정보랑
        System.out.println(authentication.getPrincipal());
        if(authentication.getPrincipal().equals(userId)) {
            return userId + " 님의 마이페이지 입니다.";
        }

        else {
            return "로그인부터 하세요.";
        }
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
