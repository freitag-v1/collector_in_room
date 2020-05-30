package swcapstone.freitag.springsecurityjpa.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenBankingClientTest {

    private String accessToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE1OTcyMjkxODAsImp0aSI6Ijg2MjVjNzgzLWY0NGQtNGNhMy1iMzM5LTAxZTNmYjRlMGI3YSJ9.UT2XwP_wlEIo-6YmZg3juwOZ88nj0AEiCXlL-YpT80s";
    private String userSeqNo = "1100758491";

    @BeforeAll
    public static void getInstance() {
        assertNotNull(OpenBankingClient.getInstance());
    }

    @Test
    public void withdraw() {
        assertTrue(OpenBankingClient.getInstance().withdraw(accessToken, Integer.parseInt(userSeqNo), "테스트", 10000));
    }

    @Test
    public void withdrawFail() {
        assertFalse(OpenBankingClient.getInstance().withdraw(accessToken, Integer.parseInt(userSeqNo), "테스트", 1000000000));
    }

    @Test
    public void deposit() {
        assertTrue(OpenBankingClient.getInstance().deposit(accessToken, Integer.parseInt(userSeqNo), "ㅇㅇㅇ", 10000));
    }

    @Test
    public void depositFail() {
        assertFalse(OpenBankingClient.getInstance().deposit(accessToken, Integer.parseInt(userSeqNo), "ㅇㅇㅇ", 1000000000));
    }

}