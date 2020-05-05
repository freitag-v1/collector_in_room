package swcapstone.freitag.springsecurityjpa.externalAPI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenBankingTest {
    @Test
    void getRealName() throws Exception {
        assertEquals("최재웅", OpenBanking.getInstance().getRealName(97, "12345678910", 930524));
    }
}