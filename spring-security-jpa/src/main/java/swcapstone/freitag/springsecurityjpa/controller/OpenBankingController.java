package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swcapstone.freitag.springsecurityjpa.externalAPI.OpenBanking;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class OpenBankingController {
    @RequestMapping("/externalapi/openbanking/oauth/token")
    public void autorizetoken(HttpServletRequest request, HttpServletResponse response)  {
        if(isAuthorizeToken(request)) {
            String authorizeToken = request.getParameter("code");
            String state = request.getParameter("state");

            try {
                Map<String, String> result = OpenBanking.getInstance().getAccessToken(authorizeToken);
                // DB에서 state가 동일한 user를 찾아서 result.get("access_token"), result.get("user_seq_no") 저장
                System.out.println("access_token:" + result.get("access_token"));
                System.out.println("user_seq_no:" + result.get("user_seq_no"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isAuthorizeToken(HttpServletRequest request) {
        return !request.getParameterMap().isEmpty();
    }
}
