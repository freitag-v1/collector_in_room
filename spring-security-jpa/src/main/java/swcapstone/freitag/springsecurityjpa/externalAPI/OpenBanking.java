package swcapstone.freitag.springsecurityjpa.externalAPI;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OpenBanking {
    private static OpenBanking instance = null;
    private static final String clientID = "4wbVEVJf0Iuj9ckTMvO9uNMVg9kdJq5kJEZ7QiHS";
    private static final String clientSecret = "lzp4N3ao3vRdpKZRW2AxtPDVv496ZV7DQQAkNNp8";
    private static final String baseURL = "https://testapi.openbanking.or.kr";
    private String accessToken;
    private String clientCode;

    private OpenBanking() throws Exception {
        APICaller getAccessToken = new APICaller("POST", baseURL + "/oauth/2.0/token");
        getAccessToken.setQueryParameter("client_id", clientID);
        getAccessToken.setQueryParameter("client_secret", clientSecret);
        getAccessToken.setQueryParameter("grant_type", "client_credentials");
        getAccessToken.setQueryParameter("scope", "oob");

        String response = getAccessToken.getResponse();
        JSONObject jResponse = new JSONObject(response);
        this.accessToken = jResponse.get("access_token").toString();
        this.clientCode = jResponse.get("client_use_code").toString();
    }

    public static OpenBanking getInstance() {
        if(instance == null) {
            try {
                instance = new OpenBanking();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public String getRealName(int userBank, String userAccount, int userBirthday) throws Exception {
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
    }

    private String getTransactionID() {
        String uniqueCode = new SimpleDateFormat("HHmmssSSS", Locale.KOREA).format(new Date());
        return String.format("%sU%s", clientCode, uniqueCode);
    }
}
