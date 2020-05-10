package swcapstone.freitag.springsecurityjpa.externalAPI;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OpenBanking {
    private static OpenBanking instance = null;
    private static final String clientID = "XXyvh2Ij7l9rss0HAVObS880qY3penX57JXkib9q";
    private static final String clientSecret = "p947iEbMvXjslhyTw4d4p2HK8U0DkOHR7o83Mtdx";
    private static final String baseURL = "https://testapi.openbanking.or.kr";
    private static final String callbackURL = "http://wodnd999999.iptime.org:8080/externalapi/openbanking/oauth/token";
    private static String clientCode = "T991627830";

    private OpenBanking() {
    }

    public static OpenBanking getInstance() {
        if(instance == null) {
            instance = new OpenBanking();
        }
        return instance;
    }

    public Map<String, String> getAccessToken(String authorizeToken) throws Exception {
        APICaller getAccessToken = new APICaller("POST", baseURL + "/oauth/2.0/token");
        getAccessToken.setHeader("content-type", "application/x-www-form-urlencoded");
        getAccessToken.setFiled("code", authorizeToken);
        getAccessToken.setFiled("client_id", clientID);
        getAccessToken.setFiled("client_secret", clientSecret);
        getAccessToken.setFiled("redirect_uri", callbackURL);
        getAccessToken.setFiled("grant_type", "authorization_code");

        String response = getAccessToken.getResponse();
        JSONObject jResponse = new JSONObject(response);
        Map<String, String> result = new HashMap<>();
        result.put("access_token", jResponse.get("access_token").toString());
        result.put("user_seq_no", jResponse.get("user_seq_no").toString());

        return result;
    }

    private String getTransactionID() {
        String uniqueCode = new SimpleDateFormat("HHmmssSSS", Locale.KOREA).format(new Date());
        return String.format("%sU%s", clientCode, uniqueCode);
    }
}
