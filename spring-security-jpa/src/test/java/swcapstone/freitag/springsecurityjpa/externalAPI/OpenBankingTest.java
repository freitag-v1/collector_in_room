package swcapstone.freitag.springsecurityjpa.externalAPI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenBankingTest {
    @Test
    void getRealName() throws Exception {
        assertEquals(OpenBanking.getInstance().getRealName(97, "12345678910", 930524), "최재웅");
    }
}