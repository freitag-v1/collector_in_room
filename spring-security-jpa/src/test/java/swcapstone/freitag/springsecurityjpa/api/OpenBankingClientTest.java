package swcapstone.freitag.springsecurityjpa.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenBankingClientTest {

    private String accessToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2MDEzOTIyNTUsImp0aSI6ImExZGRiMDk4LTQ3ZTEtNDZiNy05OGZlLThhMzE2ZDQ2OTAyNSJ9.9lk6_-nmb84O_Gl2xx5tPLH7bQWfzmE2VRamOGHTnjQ";
    private String userSeqNo = "1100758491";

    @Test
    public void withdraw() {
        assertTrue(OpenBankingClient.getInstance().withdraw(accessToken, Integer.parseInt(userSeqNo), "테스트", 10000), "오픈뱅킹에 등록된 출금이체 테스트 케이스가 실패함");
    }

    @Test
    public void withdrawFail() {
        assertFalse(OpenBankingClient.getInstance().withdraw(accessToken, Integer.parseInt(userSeqNo), "테스트", 1000000000), "오픈뱅킹에 등록되지 않은 출금이체 테스트 케이스가 성공함");
    }

    @Test
    public void deposit() {
        assertTrue(OpenBankingClient.getInstance().deposit(accessToken, Integer.parseInt(userSeqNo), "ㅇㅇㅇ", 10000), "오픈뱅킹에 등록된 입금이체 테스트 케이스가 실패함");
    }

    @Test
    public void depositFail() {
        assertFalse(OpenBankingClient.getInstance().deposit(accessToken, Integer.parseInt(userSeqNo), "ㅇㅇㅇ", 1000000000), "오픈뱅킹에 등록되지 않은 입금이체 테스트 케이스가 성공함");
    }

}