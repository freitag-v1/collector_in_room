package swcapstone.freitag.springsecurityjpa.externalAPI;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OpenBanking {
    private static OpenBanking instance;
    private static final String clientID = "IXkyzGt3C2JzogL2hqufC0YH97xx3hTJ5IFZrMDe";
    private static final String clientSecret = "HfCtT44inej7uHR1Bo5WurHsruG7pnfTWYKNQurM";
    private static final String baseURL = "https://testapi.openbanking.or.kr";
    private String accessToken;
    private String clientCode;

    private OpenBanking() throws Exception {
        URL url = new URL(baseURL + "/oauth/2.0/token" +
                "?client_id=" + clientID +
                "&client_secret=" + clientSecret +
                "&grant_type=client_credentials" +
                "&scope=oob");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        if(con.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();

            JSONObject jResponse = new JSONObject(response.toString());
            this.accessToken = jResponse.get("access_token").toString();
            this.clientCode = jResponse.get("client_use_code").toString();
        } else {
            con.disconnect();
            throw new Exception("Can not get OpenBanking access token.");
        }
    }

    public static OpenBanking getInstance() {
        if(instance == null) {
            try {
                instance = new OpenBanking();
            } catch (Exception e) {
                instance = null;
                e.printStackTrace();
            }
        }
        return instance;
    }

    public String getRealName(int userBank, String userAccount, int userBirthday) throws Exception {
        URL url = new URL(baseURL + "/v2.0/inquiry/real_name");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("bank_tran_id", getTransactionID());
        requestBody.put("bank_code_std", String.format("%03d", userBank));
        requestBody.put("account_num", userAccount);
        requestBody.put("account_holder_info_type", " ");
        requestBody.put("account_holder_info", String.valueOf(userBirthday));
        requestBody.put("tran_dtime", new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA).format(new Date()));
        con.setDoOutput(true);
        //System.out.println(new JSONObject(requestBody).toString());
        con.getOutputStream().write(new JSONObject(requestBody).toString().getBytes());

        if(con.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();

            //System.out.println(response.toString());
            JSONObject jResponse = new JSONObject(response.toString());
            return jResponse.get("account_holder_name").toString();
        } else {
            con.disconnect();
            throw new Exception("Can not get real name.");
        }
    }

    private String getTransactionID() {
        String uniqueCode = new SimpleDateFormat("HHmmssSSS", Locale.KOREA).format(new Date());
        return String.format("%sU%s", clientCode, uniqueCode);
    }
}
