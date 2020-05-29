package swcapstone.freitag.springsecurityjpa.controller;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;
import swcapstone.freitag.springsecurityjpa.api.OpenBankingClient;

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
    public String autorizetoken(HttpServletRequest request, HttpServletResponse response)  {
        if(hasAuthorizeToken(request)) {
            String authorizeToken = request.getParameter("code");
            String state = request.getParameter("state");

            try {
                Map<String, String> result = OpenBankingClient.getInstance().getAccessToken(authorizeToken);

                // DB에서 state가 동일한 user를 찾아서 access_token과 user_seq_no 저장
                Optional<UserEntity> userEntityWrapper = userRepository.findByUserOpenBankingAccessToken(state);
                userEntityWrapper.ifPresent(selectUser -> {
                    System.out.println("해당하는 유저 찾음.");
                    System.out.println("access token, user_seq_no 저장");
                    selectUser.setUserOpenBankingAccessToken("Bearer " + result.get("access_token"));
                    selectUser.setUserOpenBankingNum(Integer.parseInt(result.get("user_seq_no")));
                });
                return "<html><head><title>계좌 등록 완료</title></head><meta charset=\"UTF-8\"><body onLoad=\"setTimeout('window.close()',2000);\"><p>계좌등록이 완료되었습니다.</p></body></html>";
            } catch (Exception e) {
                e.printStackTrace();
                return "<html><head><title>계좌 등록 실패</title></head><meta charset=\"UTF-8\"><body><p>계좌등록이 실패했습니다. 다시 시도해 주세요.</p></body></html>";
            }
        }
        return null;
    }

    private boolean hasAuthorizeToken(HttpServletRequest request) {
        return !request.getParameterMap().isEmpty();
    }
}
