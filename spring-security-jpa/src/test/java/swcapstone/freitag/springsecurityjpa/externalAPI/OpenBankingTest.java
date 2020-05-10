package swcapstone.freitag.springsecurityjpa.externalAPI;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenBankingTest {

    private String accessToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE1OTY5MTcyNjksImp0aSI6ImY5ZjE3MDM1LThhNzUtNDhhNC1iY2Q2LTlkMGI3Yzk4NWE2YiJ9.pjiTiDAMJxCoJRr_45cQOb50h4e_FCcE5h_6m21oLFk";
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