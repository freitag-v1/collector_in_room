package swcapstone.freitag.springsecurityjpa.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OpenBankingClient {
    private static OpenBankingClient instance = null;
    private static final String clientID = "XXyvh2Ij7l9rss0HAVObS880qY3penX57JXkib9q";
    private static final String clientSecret = "p947iEbMvXjslhyTw4d4p2HK8U0DkOHR7o83Mtdx";
    private static final String baseURL = "https://testapi.openbanking.or.kr";
    private static final String callbackURL = "http://wodnd999999.iptime.org:8080/externalapi/openbanking/oauth/token";
    private final String oobAccountNum = "1111111111";
    private final String oobAccessToken;
    private final String clientCode;

    private OpenBankingClient() throws Exception {
        APICaller getAccessToken = new APICaller("POST", baseURL + "/oauth/2.0/token");
        getAccessToken.setQueryParameter("client_id", clientID);
        getAccessToken.setQueryParameter("client_secret", clientSecret);
        getAccessToken.setQueryParameter("grant_type", "client_credentials");
        getAccessToken.setQueryParameter("scope", "oob");

        String response = getAccessToken.getResponse();
        JSONObject jResponse = new JSONObject(response);
        this.oobAccessToken = "Bearer " + jResponse.get("access_token").toString();
        this.clientCode = jResponse.get("client_use_code").toString();
    }

    public static OpenBankingClient getInstance() {
        if(instance == null) {
            try {
                instance = new OpenBankingClient();
            } catch (Exception e) {
                e.printStackTrace();
                instance = null;
            }
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

    public boolean withdraw(String accessToken, int userSeqNo, String memo, int amount) {
        try {
            Map<String, String> account = getAccount(accessToken, userSeqNo);
            APICaller withdraw = new APICaller("POST", baseURL + "/v2.0/transfer/withdraw/fin_num");
            withdraw.setHeader("Authorization", accessToken);
            withdraw.setJsonBody("bank_tran_id", getTransactionID());
            withdraw.setJsonBody("cntr_account_type", "N");
            withdraw.setJsonBody("cntr_account_num", oobAccountNum);
            withdraw.setJsonBody("dps_print_content", memo);
            withdraw.setJsonBody("fintech_use_num", account.get("fintech_use_num"));
            withdraw.setJsonBody("tran_amt", String.valueOf(amount));
            withdraw.setJsonBody("tran_dtime", getTransactionTime());
            withdraw.setJsonBody("req_client_name", account.get("account_holder_name"));
            withdraw.setJsonBody("req_client_fintech_use_num", account.get("fintech_use_num"));
            withdraw.setJsonBody("req_client_num", String.valueOf(userSeqNo));
            withdraw.setJsonBody("transfer_purpose", "TR");
            withdraw.setJsonBody("recv_client_name", "방구석 수집가");
            withdraw.setJsonBody("recv_client_bank_code", "097");
            withdraw.setJsonBody("recv_client_account_num", oobAccountNum);

            String response = withdraw.getResponse();
            JSONObject jResponse = new JSONObject(response);
            if(jResponse.get("rsp_code").equals("A0000")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deposit(String accessToken, int userSeqNo, String memo, int amount) {
        try {
            Map<String, String> account = getAccount(accessToken, userSeqNo);
            APICaller deposit = new APICaller("POST", baseURL + "/v2.0/transfer/deposit/fin_num");
            deposit.setHeader("Authorization", oobAccessToken);
            deposit.setJsonBody("cntr_account_type", "N");
            deposit.setJsonBody("cntr_account_num", oobAccountNum);
            deposit.setJsonBody("wd_pass_phrase", "NONE");
            deposit.setJsonBody("wd_print_content", memo);
            deposit.setJsonBody("tran_dtime", getTransactionTime());
            deposit.setJsonBody("req_cnt", "1");
            deposit.setJsonBody("req_list", makeDepositRequest(account, userSeqNo, memo, amount));

            String response = deposit.getResponse();
            JSONObject jResponse = new JSONObject(response);
            if(jResponse.get("rsp_code").equals("A0000")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA).format(new Date());
    }

    private JSONArray makeDepositRequest(Map<String, String> account, int userSeqNo, String memo, int amount) {
        JSONObject depositRequest = new JSONObject();
        depositRequest.put("tran_no", "1");
        depositRequest.put("bank_tran_id", getTransactionID());
        depositRequest.put("fintech_use_num", account.get("fintech_use_num"));
        depositRequest.put("print_content", memo);
        depositRequest.put("tran_amt", amount);
        depositRequest.put("req_client_name", account.get("account_holder_name"));
        depositRequest.put("req_client_fintech_use_num", account.get("fintech_use_num"));
        depositRequest.put("req_client_num", String.valueOf(userSeqNo));
        depositRequest.put("transfer_purpose", "TR");
        return new JSONArray().put(depositRequest);
    }
}