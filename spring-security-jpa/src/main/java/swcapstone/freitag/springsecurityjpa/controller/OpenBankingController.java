package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swcapstone.freitag.springsecurityjpa.externalAPI.APICaller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@RestController
public class OpenBankingController {
    @RequestMapping("/externalapi/authorizetoken")
    public void autorizetoken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("==========authorize==========");
        Map<String, String[]> parametes = request.getParameterMap();
        Iterator<String> keys = parametes.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            System.out.println(key + ":");
            for(int i = 0; i < parametes.get(key).length; i++) {
                System.out.println(parametes.get(key)[i]);
            }
        }
        APICaller accessToken = new APICaller("POST", "https://testapi.openbanking.or.kr/oauth/2.0/token");
        accessToken.setHeader("content-type", "application/x-www-form-urlencoded");
        accessToken.setFiled("code", parametes.get("code")[0]);
        accessToken.setFiled("client_id", "4wbVEVJf0Iuj9ckTMvO9uNMVg9kdJq5kJEZ7QiHS");
        accessToken.setFiled("client_secret", "lzp4N3ao3vRdpKZRW2AxtPDVv496ZV7DQQAkNNp8");
        accessToken.setFiled("redirect_uri", "http://wodnd999999.iptime.org:8080/externalapi/authorizetoken");
        accessToken.setFiled("grant_type", "authorization_code");
        try {
            System.out.println(accessToken.getResponse());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //다르면 안됨...
    /*@RequestMapping("/externalapi/accesstoken")
    public void accesstoken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("==========access==========");
        Map<String, String[]> parametes = request.getParameterMap();
        Iterator<String> keys = parametes.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            for(int i = 0; i < parametes.get(key).length; i++) {
                System.out.println(parametes.get(key)[i]);
            }
        }
    }*/
}
