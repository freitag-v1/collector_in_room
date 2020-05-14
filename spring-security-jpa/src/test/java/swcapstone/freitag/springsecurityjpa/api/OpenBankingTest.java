package swcapstone.freitag.springsecurityjpa.api;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import swcapstone.freitag.springsecurityjpa.api.OpenBanking;

import static org.junit.jupiter.api.Assertions.*;

class OpenBankingTest {

    private String accessToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE1OTcyMjkxODAsImp0aSI6Ijg2MjVjNzgzLWY0NGQtNGNhMy1iMzM5LTAxZTNmYjRlMGI3YSJ9.UT2XwP_wlEIo-6YmZg3juwOZ88nj0AEiCXlL-YpT80s";
    private String userSeqNo = "1100758491";

    @Test
    public void withdraw() {
        assertTrue(OpenBanking.getInstance().withdraw(accessToken, Integer.parseInt(userSeqNo), "1111122222", "테스트", 10000));
    }

    @Test
    public void withdrawTooMuchFail() {
        assertFalse(OpenBanking.getInstance().withdraw(accessToken, Integer.parseInt(userSeqNo), "1111122222", "테스트", 100000));
    }

}