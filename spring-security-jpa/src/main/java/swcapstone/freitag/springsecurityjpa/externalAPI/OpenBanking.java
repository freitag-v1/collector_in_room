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
    //private String accessToken;
    private static String clientCode = "T991627830";

    private OpenBanking() {
        /*APICaller getAccessToken = new APICaller("POST", baseURL + "/oauth/2.0/token");
        getAccessToken.setQueryParameter("client_id", clientID);
        getAccessToken.setQueryParameter("client_secret", clientSecret);
        getAccessToken.setQueryParameter("grant_type", "client_credentials");
        getAccessToken.setQueryParameter("scope", "oob");

        String response = getAccessToken.getResponse();
        JSONObject jResponse = new JSONObject(response);
        this.accessToken = jResponse.get("access_token").toString();
        this.clientCode = jResponse.get("client_use_code").toString();*/
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

    /*public String getRealName(int userBank, String userAccount, int userBirthday) throws Exception {
        APICaller getRealName = new APICaller("POST", baseURL + "/v2.0/inquiry/real_name");
        getRealName.setHeader("Authorization", "Bearer " + accessToken);
        getRealName.setJsonBody("bank_tran_id", getTransactionID());
        getRealName.setJsonBody("bank_code_std", String.format("%03d", userBank));
        getRealName.setJsonBody("account_num", userAccount);
        getRealName.setJsonBody("account_holder_info_type", " ");
        getRealName.setJsonBody("account_holder_info", String.format("%06d", userBirthday));
        getRealName.setJsonBody("tran_dtime", new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA).format(new Date()));

        String response = getRealName.getResponse();
        JSONObject jResponse = new JSONObject(response);
        return jResponse.get("account_holder_name").toString();
    }*/

    private String getTransactionID() {
        String uniqueCode = new SimpleDateFormat("HHmmssSSS", Locale.KOREA).format(new Date());
        return String.format("%sU%s", clientCode, uniqueCode);
    }
}
