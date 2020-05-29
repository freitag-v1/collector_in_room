package swcapstone.freitag.springsecurityjpa.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SMSTest {
    @Test
    public void sendSMS() throws IOException {
        new SMS().sendSMS("01027540421", "test");
    }
}