package swcapstone.freitag.springsecurityjpa.controller;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.utils.Common;
import swcapstone.freitag.springsecurityjpa.utils.Repositories;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static swcapstone.freitag.springsecurityjpa.utils.Common.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Repositories repositories;

    private String userId;
    private String userPassword;

    @BeforeAll
    static void setupSharedFixture(@Autowired DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("UserControllerSharedFixture.sql"));
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @BeforeEach
    void setUp() {
        userId = "normal_user";
        userPassword = Common.SHA256("freitag123!");
    }

    @Test
    public void successfulLogin() throws Exception {
        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().exists("Authorization"));
    }

    @Test
    public void loginWithNotSignedUpId() throws Exception {
        // Setup Fixture
        userId = "not_signed_up_user";

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    public void loginWithWrongPassword() throws Exception {
        // Setup Fixture
        userPassword = Common.SHA256("1234");

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    public void loginAndGetReward1() throws Exception {
        // Setup Fixture
        UserEntity fixtureUserEntity = repositories.getUserEntity(userId);
        fixtureUserEntity.setUserVisit(0);
        fixtureUserEntity.setUserLastVisit(new Timestamp(0));
        repositories.saveUserEntity(fixtureUserEntity);

        UserEntity expectedUserEntity = makeEmptyUserEntity();
        expectedUserEntity.setTotalPoint(fixtureUserEntity.getTotalPoint() + 100);
        expectedUserEntity.setPoint(fixtureUserEntity.getTotalPoint() + 100);
        expectedUserEntity.setUserVisit(fixtureUserEntity.getUserVisit() + 1);
        expectedUserEntity.setUserLastVisit(new Timestamp(System.currentTimeMillis()));

        // Exercise SUT
        ResultActions result = performLogin(this.userId, userPassword);

        // Verify Outcome
        result.andExpect(header().exists("Authorization"))
                .andExpect(header().stringValues("reward", "true"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void loginAndGetReward2() throws Exception {
        // Setup Fixture
        UserEntity fixtureUserEntity = repositories.getUserEntity(userId);
        fixtureUserEntity.setUserVisit(29);
        fixtureUserEntity.setUserLastVisit(new Timestamp(0));
        repositories.saveUserEntity(fixtureUserEntity);

        UserEntity expectedUserEntity = makeEmptyUserEntity();
        expectedUserEntity.setTotalPoint(fixtureUserEntity.getTotalPoint() + 100);
        expectedUserEntity.setPoint(fixtureUserEntity.getTotalPoint() + 100);
        expectedUserEntity.setUserVisit(fixtureUserEntity.getUserVisit() + 1);
        expectedUserEntity.setUserLastVisit(new Timestamp(System.currentTimeMillis()));

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().exists("Authorization"))
                .andExpect(header().stringValues("reward", "true"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void loginButNotGetReward() throws Exception {
        // Setup Fixture
        UserEntity fixtureUserEntity = repositories.getUserEntity(userId);
        fixtureUserEntity.setUserVisit(30);
        repositories.saveUserEntity(fixtureUserEntity);

        UserEntity expectedUserEntity = copyUserEntity(fixtureUserEntity);

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().exists("Authorization"))
                .andExpect(header().doesNotExist("reward"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void successfulSignUp() throws Exception {
        // Setup Fixture
        userId = "other_user";
        UserEntity fixtureUserEntity = makeEmptyUserEntity();
        fixtureUserEntity.setUserId(userId);
        fixtureUserEntity.setUserPassword(Common.SHA256("freitag321#"));
        fixtureUserEntity.setUserName("최재웅");
        fixtureUserEntity.setUserPhone("01027540421");
        fixtureUserEntity.setUserEmail("wodnd999999@ajou.ac.kr");
        fixtureUserEntity.setUserAffiliation("AjouSW");

        UserEntity expectedUserEntity = copyUserEntity(fixtureUserEntity);

        // Exercise SUT
        ResultActions result = performSignUp(fixtureUserEntity);

        // Verify Outcome
        result.andExpect(header().string("signup", "success"))
                .andExpect(header().exists("state"));
        String expectedState = result.andReturn().getResponse().getHeader("state");

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserInfoEquals(expectedUserEntity, actualUserEntity);
        assertEquals(expectedState, actualUserEntity.getUserOpenBankingAccessToken());
    }

    @Test
    public void signUpWithUserIdInUse() throws Exception {
        // Setup Fixture
        UserEntity fixtureUserEntity = makeEmptyUserEntity();
        fixtureUserEntity.setUserId(userId);
        fixtureUserEntity.setUserPassword(Common.SHA256("freitag321#"));
        fixtureUserEntity.setUserName("최재웅");
        fixtureUserEntity.setUserPhone("01027540421");
        fixtureUserEntity.setUserEmail("wodnd999999@ajou.ac.kr");
        fixtureUserEntity.setUserAffiliation("AjouSW");

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performSignUp(fixtureUserEntity);

        // Verify Outcome
        result.andExpect(header().string("signup", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserInfoEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void successfulMypage() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(userId);

        // Exercise SUT
        ResultActions result = performMypage(authorization);

        // Verify Outcome
        String content = result.andReturn().getResponse().getContentAsString();
        assertNotEquals("", content);
    }

    @Test
    public void mypageWithoutAuthorizationToken() throws Exception {
        // Setup Fixture
        String authorization = null;

        // Exercise SUT
        ResultActions result = performMypage(authorization);

        // Verify Outcome
        String content = result.andReturn().getResponse().getContentAsString();
        assertEquals("", content);
    }

    @Test
    public void mypageWithExpiredAuthorizationToken() throws Exception {
        // Setup Fixture
        String authorization = makeExpiredAuthorizationToken(userId);

        // Exercise SUT
        ResultActions result = performMypage(authorization);

        // Verify Outcome
        String content = result.andReturn().getResponse().getContentAsString();
        assertEquals("", content);
    }

    @Test
    public void mypageUpdate() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(userId);
        UserEntity fixtureUserEntity = makeEmptyUserEntity();
        fixtureUserEntity.setUserId(userId);
        fixtureUserEntity.setUserName("개명함");
        fixtureUserEntity.setUserPhone("01099991111");
        fixtureUserEntity.setUserEmail("ung27540421@outlook.com");
        fixtureUserEntity.setUserAffiliation("Korea");

        UserEntity expectedUserEntity = copyUserEntity(fixtureUserEntity);

        // Exercise SUT
        ResultActions result = performMypageUpdate(authorization, fixtureUserEntity);

        // Verify Outcome
        result.andExpect(header().string("update", "success"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserInfoEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void mypageUpdateWithoutLogin() throws Exception {
        // Setup Fixture
        String authorization = null;
        UserEntity fixtureUserEntity = makeEmptyUserEntity();
        fixtureUserEntity.setUserName("개명함");
        fixtureUserEntity.setUserPhone("01099991111");
        fixtureUserEntity.setUserEmail("ung27540421@outlook.com");
        fixtureUserEntity.setUserAffiliation("Korea");

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performMypageUpdate(authorization, fixtureUserEntity);

        // Verify Outcome
        result.andExpect(header().string("update", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserInfoEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void mypageUpdateWithExpiredAuthorizationToken() throws Exception {
        // Setup Fixture
        String authorization = makeExpiredAuthorizationToken(userId);
        UserEntity fixtureUserEntity = makeEmptyUserEntity();
        fixtureUserEntity.setUserName("개명함");
        fixtureUserEntity.setUserPhone("01099991111");
        fixtureUserEntity.setUserEmail("ung27540421@outlook.com");
        fixtureUserEntity.setUserAffiliation("Korea");

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performMypageUpdate(authorization, fixtureUserEntity);

        // Verify Outcome
        result.andExpect(header().string("update", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserInfoEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void successfulExchangePoint() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(userId);
        Integer amount = 10000;

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));
        expectedUserEntity.setPoint(expectedUserEntity.getPoint() - amount);

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "success"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertTrue(0 <= actualUserEntity.getPoint());
        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void exchangePointWithoutRegisteringOpenBanking() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(userId);
        Integer amount = 10000;
        UserEntity fixtureUserEntity = repositories.getUserEntity(userId);
        fixtureUserEntity.setUserOpenBankingAccessToken(UUID.randomUUID().toString().replace("-", ""));
        fixtureUserEntity.setUserOpenBankingNum(0);
        repositories.saveUserEntity(fixtureUserEntity);

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void exchangePointMoreThanHaving() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(userId);
        Integer amount = 1000000;

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void exchangePointButOpenBankingError() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(userId);
        Integer amount = 10000;
        UserEntity fixtureUserEntity = repositories.getUserEntity(userId);
        fixtureUserEntity.setUserOpenBankingAccessToken(makeExpiredAuthorizationToken(userId));
        repositories.saveUserEntity(fixtureUserEntity);

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void exchangePointWithoutLogin() throws Exception {
        // Setup Fixture
        String authorization = null;
        Integer amount = 10000;

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void exchangePointWithExpiredAuthorizationToken() throws Exception {
        // Setup Fixture
        String authorization = makeExpiredAuthorizationToken(userId);
        Integer amount = 10000;

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void exchangePointWithoutAmount() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(userId);
        Integer amount = null;

        UserEntity expectedUserEntity = copyUserEntity(repositories.getUserEntity(userId));

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(userId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void top5RichUser() throws Exception {
        // Exercise SUT
        ResultActions result = performRankingByPoint();

        // Verify Outcome
        result.andExpect(header().string("ranking", "success"));
        // more...?
    }

    @Test
    public void top5SmartUser() throws Exception {
        // Exercise SUT
        ResultActions result = performRankingByAccuracy();

        // Verify Outcome
        result.andExpect(header().string("ranking", "success"));
        // more...?
    }

    private ResultActions performLogin(String userId, String userPassword) throws Exception {
        URI uri = new URIBuilder("/api/login")
                .setParameter("userId", userId)
                .setParameter("userPassword", userPassword)
                .build();
        return mockMvc.perform(MockMvcRequestBuilders.post(uri));
    }

    private ResultActions performSignUp(UserEntity fixtureUserEntity) throws Exception {
        URI uri = new URIBuilder("/api/signup")
                .setParameter("userId", fixtureUserEntity.getUserId())
                .setParameter("userPassword", fixtureUserEntity.getUserPassword())
                .setParameter("userName", fixtureUserEntity.getUserName())
                .setParameter("userPhone", fixtureUserEntity.getUserPhone())
                .setParameter("userEmail", fixtureUserEntity.getUserEmail())
                .setParameter("userAffiliation", fixtureUserEntity.getUserAffiliation())
                .build();
        return mockMvc.perform(MockMvcRequestBuilders.put(uri));
    }

    private ResultActions performMypage(String authorization) throws Exception {
        URI uri = new URIBuilder("/api/mypage")
                .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performMypageUpdate(String authorization, UserEntity fixtureUserEntity) throws Exception {
        URI uri = new URIBuilder("/api/mypage/update")
                .addParameter("userName", fixtureUserEntity.getUserName())
                .addParameter("userPhone", fixtureUserEntity.getUserPhone())
                .addParameter("userEmail", fixtureUserEntity.getUserEmail())
                .addParameter("userAffiliation", fixtureUserEntity.getUserAffiliation())
                .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performExchangePoint(String authorization, Integer amount) throws Exception {
        URI uri = new URIBuilder("/api/mypage/exchange")
                .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(uri);
        if(authorization != null) {
            request.header("authorization", authorization);
        }
        if(amount != null) {
            request.header("amount", amount);
        }

        return mockMvc.perform(request);
    }

    private ResultActions performRankingByPoint() throws Exception {
        URI uri = new URIBuilder("/api/ranking/point")
                .build();
        return mockMvc.perform(MockMvcRequestBuilders.put(uri));
    }

    private ResultActions performRankingByAccuracy() throws Exception {
        URI uri = new URIBuilder("/api/ranking/accuracy")
                .build();
        return mockMvc.perform(MockMvcRequestBuilders.put(uri));
    }

    private void assertUserPointEquals(UserEntity expectedUserEntity, UserEntity actualUserEntity) {
        assertEquals(expectedUserEntity.getTotalPoint(), actualUserEntity.getTotalPoint());
        assertEquals(expectedUserEntity.getPoint(), actualUserEntity.getPoint());
        assertEquals(expectedUserEntity.getUserVisit(), actualUserEntity.getUserVisit());
        // 오차 10초 허용
        assertTrue(10 * 1000 >= Math.abs(expectedUserEntity.getUserLastVisit().getTime() - actualUserEntity.getUserLastVisit().getTime()));
    }

    private void assertUserInfoEquals(UserEntity expectedUserEntity, UserEntity actualUserEntity) {
        assertEquals(expectedUserEntity.getUserId(), actualUserEntity.getUserId());
        assertEquals(expectedUserEntity.getUserName(), actualUserEntity.getUserName());
        assertEquals(expectedUserEntity.getUserPhone(), actualUserEntity.getUserPhone());
        assertEquals(expectedUserEntity.getUserEmail(), actualUserEntity.getUserEmail());
        assertEquals(expectedUserEntity.getUserAffiliation(), actualUserEntity.getUserAffiliation());
    }
}