package swcapstone.freitag.springsecurityjpa.api;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;
import swcapstone.freitag.springsecurityjpa.service.UserService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Optional;

@Service
public class SMSClient {
    @Autowired
    private UserRepository userRepository;

    private final String sms_url = "https://sslsms.cafe24.com/sms_sender.php";
    private final String user_id = "ung27540421";
    private final String secure = "5f9b554d4fc406f8a56a4f280638ec33";
    private final String sphone1 = "010";
    private final String sphone2 = "2754";
    private final String sphone3 = "0421";
    private final String smsType = "S";
    private String testflag = "Y";

    private String nullCheck(String str) {
        String result;
        if (str == null) {
            result = "";
        } else if (str.equals("")) {
            result = "";
        } else {
            result = str;
        }
        return result;
    }

    public void setTestflag(String testflag) {
        this.testflag = testflag;
    }

    private String base64Encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public boolean sendSMS(String userId, String msg) throws IOException {
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        String rphone = userEntityWrapper.get().getUserPhone();

        HttpClient httpClient = new DefaultHttpClient();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        builder.addTextBody("user_id", base64Encode(user_id));
        builder.addTextBody("secure", base64Encode(secure));
        builder.addTextBody("msg", base64Encode(nullCheck(msg)));
        builder.addTextBody("rphone", base64Encode(nullCheck(rphone)));
        builder.addTextBody("sphone1", base64Encode(sphone1));
        builder.addTextBody("sphone2", base64Encode(sphone2));
        builder.addTextBody("sphone3", base64Encode(sphone3));
        builder.addTextBody("mode", base64Encode("1"));
        builder.addTextBody("testflag", base64Encode(testflag));
        builder.addTextBody("smsType", base64Encode(smsType));

        HttpPost httpPost = new HttpPost(sms_url);
        httpPost.setEntity(builder.build());
        HttpResponse httpResponse = httpClient.execute(httpPost);

        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String tmpMsg = EntityUtils.toString(httpResponse.getEntity());
            String[] rMsg = tmpMsg.split(",");
            String Result = rMsg[0]; //발송결과
            String Count = ""; //잔여건수
            if (rMsg.length > 1) {
                Count = rMsg[1];
            }

            //발송결과 알림
            switch (Result) {
                case "success":
                    System.out.println("성공적으로 발송하였습니다.");
                    System.out.println("잔여건수는 " + Count + "건 입니다.");
                    return true;
                case "3205":
                    System.out.println("잘못된 번호형식입니다.");
                    return false;
                default:
                    System.out.println("[Error]" + Result);
                    return false;
            }
        } else {
            return false;
        }
    }
}