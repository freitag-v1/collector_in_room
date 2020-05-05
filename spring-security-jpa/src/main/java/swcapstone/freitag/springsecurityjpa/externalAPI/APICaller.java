package swcapstone.freitag.springsecurityjpa.externalAPI;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class APICaller {
    private String method;
    private String url;
    private Map<String, String> queryParameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> jsonBody = new HashMap<>();

    public APICaller(String method, String baseURL) {
        url = baseURL;
        this.method = method;
    }

    public void setQueryParameter(String key, String value) {
        queryParameters.put(key, value);
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setJsonBody(String key, String value) {
        jsonBody.put(key, value);
    }

    public String getResponse() throws Exception {
        if(!queryParameters.isEmpty()) {
            Iterator<String> iterator = queryParameters.keySet().iterator();
            String key = iterator.next();
            url += "?" + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(queryParameters.get(key), "UTF-8");
            while(iterator.hasNext()) {
                key = iterator.next();
                url += "&" + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(queryParameters.get(key), "UTF-8");
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
