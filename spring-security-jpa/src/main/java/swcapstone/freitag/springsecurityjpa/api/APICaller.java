package swcapstone.freitag.springsecurityjpa.api;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class APICaller {
    private String method;
    private String url;
    private ArrayList<AbstractMap.SimpleEntry<String, String>> queryParameters = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> jsonBody = new HashMap<>();
    private ArrayList<AbstractMap.SimpleEntry<String, String>> filed = new ArrayList<>();

    public APICaller(String method, String baseURL) {
        url = baseURL;
        this.method = method;
    }

    public void setQueryParameter(String key, String value) {
        queryParameters.add(new AbstractMap.SimpleEntry<>(key, value));
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setJsonBody(String key, String value) {
        jsonBody.put(key, value);
    }

    public void setFiled(String key, String value) {
        filed.add(new AbstractMap.SimpleEntry<>(key, value));
    }

    public String getResponse() throws Exception {
        if(!queryParameters.isEmpty()) {
            for(int i = 0; i < queryParameters.size(); i++) {
                if(i > 0) {
                    url += "&";
                } else {
                    url += "?";
                }
                url += URLEncoder.encode(queryParameters.get(i).getKey(), "UTF-8") + "=" + URLEncoder.encode(queryParameters.get(i).getValue(), "UTF-8");
            }
        }

        HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
        con.setRequestMethod(method);

        if(!headers.isEmpty()) {
            for(String key : headers.keySet()) {
                con.setRequestProperty(key, headers.get(key));
            }
        }

        if(!jsonBody.isEmpty()) {
            con.setDoOutput(true);
            con.getOutputStream().write(new JSONObject(jsonBody).toString().getBytes());
        } else if(!filed.isEmpty()) {
            String body = "";
            for (int i = 0; i < filed.size(); i++) {
                if (i > 0) {
                    body += "&";
                }
                body += URLEncoder.encode(filed.get(i).getKey(), "UTF-8") + "=" + URLEncoder.encode(filed.get(i).getValue(), "UTF-8");
            }
            System.out.println(body);
            con.setDoOutput(true);
            con.getOutputStream().write(body.getBytes());
        }

        int responseCode = con.getResponseCode();
        if(responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();

            return response.toString();
        } else {
            con.disconnect();
            throw new Exception(responseCode + " Error");
        }
    }
}
