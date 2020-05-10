package swcapstone.freitag.springsecurityjpa.controller;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swcapstone.freitag.springsecurityjpa.domain.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.UserRepository;
import swcapstone.freitag.springsecurityjpa.externalAPI.OpenBanking;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
public class OpenBankingController {

    private UserRepository userRepository;

    @RequestMapping("/externalapi/openbanking/oauth/token")
    @Transactional
    public void autorizetoken(HttpServletRequest request, HttpServletResponse response)  {
        if(hasAuthorizeToken(request)) {
            String authorizeToken = request.getParameter("code");
            String state = request.getParameter("state");

            try {
                Map<String, String> result = OpenBanking.getInstance().getAccessToken(authorizeToken);

                // DB에서 state가 동일한 user를 찾아서 access_token과 user_seq_no 저장
                Optional<UserEntity> userEntityWrapper = userRepository.findByUserOpenBankingAccessToken(state);
                userEntityWrapper.ifPresent(selectUser -> {
                    System.out.println("해당하는 유저 찾음.");
                    System.out.println("access token, user_seq_no 저장");
                    selectUser.setUserOpenBankingAccessToken(result.get("access_token"));
                    selectUser.setUserOpenBankingNum(Integer.parseInt(result.get("user_seq_no")));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean hasAuthorizeToken(HttpServletRequest request) {
        return !request.getParameterMap().isEmpty();
    }
}
