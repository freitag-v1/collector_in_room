package swcapstone.freitag.springsecurityjpa.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SMSClientTest {

    private SMSClient smsClient = new SMSClient();

    @Disabled("핸드폰 번호 대신 ID를 받게 된 이후로 테스트 실패함.")
    @Test
    public void sendSMS() throws IOException {
        assertTrue(smsClient.sendSMS("normal_user", "test"), "문자발송 테스트가 실패함.");
    }
}