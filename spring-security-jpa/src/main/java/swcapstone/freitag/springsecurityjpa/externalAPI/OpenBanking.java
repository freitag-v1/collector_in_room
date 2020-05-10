package swcapstone.freitag.springsecurityjpa.externalAPI;

import org.json.JSONArray;
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

    public boolean withdraw(String accessToken, int userSeqNo, String AccountNum, String memo, int amount) {
        try {
            Map<String, String> account = getAccount(accessToken, userSeqNo);
            APICaller withdraw = new APICaller("POST", baseURL + "/v2.0/transfer/withdraw/fin_num");
            withdraw.setHeader("Authorization", accessToken);
            withdraw.setJsonBody("bank_tran_id", getTransactionID());
            withdraw.setJsonBody("cntr_account_type", "N");
            withdraw.setJsonBody("cntr_account_num", "1111111111");
            withdraw.setJsonBody("dps_print_content", memo);
            withdraw.setJsonBody("fintech_use_num", account.get("fintech_use_num"));
            withdraw.setJsonBody("tran_amt", String.valueOf(amount));
            withdraw.setJsonBody("tran_dtime", getTransactionTime());
            withdraw.setJsonBody("req_client_name", account.get("account_holder_name"));
            withdraw.setJsonBody("req_client_bank_code", account.get("bank_code_std"));
            withdraw.setJsonBody("req_client_account_num", AccountNum);
            withdraw.setJsonBody("req_client_num", String.valueOf(userSeqNo));
            withdraw.setJsonBody("transfer_purpose", "TR");
            withdraw.setJsonBody("recv_client_name", "방구석 수집가");
            withdraw.setJsonBody("recv_client_bank_code", "097");
            withdraw.setJsonBody("recv_client_account_num", "1111111111");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Map<String, String> getAccount(String accessToken, int userSeqNo) throws Exception {
        APICaller getAccount = new APICaller("GET", baseURL + "/v2.0/account/list");
        getAccount.setHeader("Authorization", accessToken);
        getAccount.setQueryParameter("user_seq_no", String.valueOf(userSeqNo));
        getAccount.setQueryParameter("include_cancel_yn", "N");
        getAccount.setQueryParameter("sort_order", "D");

        String response = getAccount.getResponse();
        JSONObject jResponse = new JSONObject(response);
        JSONArray resList = (JSONArray) jResponse.get("res_list");
        JSONObject account = (JSONObject) resList.get(0);
        Map<String, String> result = new HashMap<>();
        result.put("fintech_use_num", account.get("fintech_use_num").toString());
        result.put("bank_code_std", account.get("bank_code_std").toString());
        result.put("account_holder_name", account.get("account_holder_name").toString());

        return result;
    }

    private String getTransactionID() {
        String uniqueCode = new SimpleDateFormat("HHmmssSSS", Locale.KOREA).format(new Date());
        return String.format("%sU%s", clientCode, uniqueCode);
    }

    private String getTransactionTime() {
        return new SimpleDateFormat("yyyyMMHHmmss", Locale.KOREA).format(new Date());
    }
}
