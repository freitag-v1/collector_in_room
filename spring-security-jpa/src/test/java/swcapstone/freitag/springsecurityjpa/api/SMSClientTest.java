package swcapstone.freitag.springsecurityjpa.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SMSClientTest {
    @Test
    public void sendSMS() throws IOException {
        new SMSClient().sendSMS("01027540421", "test");
    }
}