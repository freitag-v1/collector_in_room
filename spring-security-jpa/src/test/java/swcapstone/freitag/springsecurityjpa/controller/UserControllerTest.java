package swcapstone.freitag.springsecurityjpa.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import sun.nio.ch.Util;
import swcapstone.freitag.springsecurityjpa.utils.JwtProperties;
import swcapstone.freitag.springsecurityjpa.utils.Utils;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private final String userId = "normal_user";
    private final String userPassword = Utils.SHA256("freitag123!");

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
        String userId = "not_signed_up_user";

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    public void loginWithWrongPassword() throws Exception {
        String userPassword = Utils.SHA256("1234");

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    public void loginAndGetReward1() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        int totalPoint = userEntityWrapper.get().getTotalPoint();
        int point = userEntityWrapper.get().getPoint();
        userEntityWrapper.ifPresent(userEntity -> {
            userEntity.setUserVisit(0);
            userEntity.setUserLastVisit(new Timestamp(0));
            userRepository.save(userEntity);
        });

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().exists("Authorization"))
                .andExpect(header().stringValues("reward", "true"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("총 획득 포인트가 제대로 변경되지 않음", totalPoint + 100, userEntity.getTotalPoint());
            assertEquals("현재 포인트가 제대로 변경되지 않음", point + 100, userEntity.getPoint());
            assertEquals("방문 일수가 제대로 변경되지 않음", 1, userEntity.getUserVisit());
            assertNotEquals("마지막 방문일이 제대로 변경되지 않음", new Timestamp(0), userEntity.getUserLastVisit());
        });
    }

    @Test
    public void loginAndGetReward2() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        int totalPoint = userEntityWrapper.get().getTotalPoint();
        int point = userEntityWrapper.get().getPoint();
        userEntityWrapper.ifPresent(userEntity -> {
            userEntity.setUserVisit(29);
            userEntity.setUserLastVisit(new Timestamp(0));
            userRepository.save(userEntity);
        });

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().exists("Authorization"))
                .andExpect(header().stringValues("reward", "true"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("총 획득 포인트가 제대로 변경되지 않음", totalPoint + 100, userEntity.getTotalPoint());
            assertEquals("현재 포인트가 제대로 변경되지 않음", point + 100, userEntity.getPoint());
            assertEquals("방문 일수가 제대로 변경되지 않음", 30, userEntity.getUserVisit());
            assertNotEquals("마지막 방문일이 제대로 변경되지 않음", new Timestamp(0), userEntity.getUserLastVisit());
        });
    }

    @Test
    public void loginButNotGetReward() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        int totalPoint = userEntityWrapper.get().getTotalPoint();
        int point = userEntityWrapper.get().getPoint();
        userEntityWrapper.ifPresent(userEntity -> {
            userEntity.setUserVisit(30);
            userEntity.setUserLastVisit(new Timestamp(0));
            userRepository.save(userEntity);
        });

        // Exercise SUT
        ResultActions result = performLogin(userId, userPassword);

        // Verify Outcome
        result.andExpect(header().exists("Authorization"))
                .andExpect(header().doesNotExist("reward"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("총 획득 포인트가 변경됨", totalPoint, userEntity.getTotalPoint());
            assertEquals("현재 포인트가 변경됨", point, userEntity.getPoint());
        });
    }

    private ResultActions performLogin(String userId, String userPassword) throws Exception {
        URI uri = new URIBuilder("/api/login")
                .setParameter("userId", userId)
                .setParameter("userPassword", userPassword)
                .build();
        return mockMvc.perform(MockMvcRequestBuilders.post(uri));
    }

    @Test
    public void successfulSignUp() throws Exception {
        // Setup Fixture
        String userId = "other_user";
        String userPassword = Utils.SHA256("freitag321#");
        String userName = "최재웅";
        String userPhone = "01027540421";
        String userEmail = "wodnd999999@ajou.ac.kr";
        String userAffiliation = "AjouSW";

        // Exercise SUT
        ResultActions result = performSignUp(userId, userPassword, userName, userPhone, userEmail, userAffiliation);

        // Verify Outcome
        result.andExpect(header().string("signup", "success"))
                .andExpect(header().exists("state"));
        String state = result.andReturn().getResponse().getHeader("state");
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("userId가 다르게 저장됨", userId, userEntity.getUserId());
            assertEquals("userName이 다르게 저장됨", userName, userEntity.getUserName());
            assertEquals("userPhone이 다르게 저장됨", userPhone, userEntity.getUserPhone());
            assertEquals("userEmail이 다르게 저장됨", userEmail, userEntity.getUserEmail());
            assertEquals("userAffiliation이 다르게 저장됨", userAffiliation, userEntity.getUserAffiliation());
            assertEquals("받은 state가 DB와 다름", state, userEntity.getUserOpenBankingAccessToken());
        });
    }

    @Test
    public void signUpWithUserIdInUse() throws Exception {
        // Setup Fixture
        String userPassword = Utils.SHA256("freitag321#");
        String userName = "최재웅";
        String userPhone = "01027540421";
        String userEmail = "wodnd999999@ajou.ac.kr";
        String userAffiliation = "AjouSW";

        // Exercise SUT
        ResultActions result = performSignUp(userId, userPassword, userName, userPhone, userEmail, userAffiliation);

        // Verify Outcome
        result.andExpect(header().string("signup", "fail"));
    }

    private ResultActions performSignUp(String userId, String userPassword, String userName, String userPhone, String userEmail, String userAffiliation) throws Exception {
        URI uri = new URIBuilder("/api/signup")
                .setParameter("userId", userId)
                .setParameter("userPassword", userPassword)
                .setParameter("userName", userName)
                .setParameter("userPhone", userPhone)
                .setParameter("userEmail", userEmail)
                .setParameter("userAffiliation", userAffiliation)
                .build();
        return mockMvc.perform(MockMvcRequestBuilders.put(uri));
    }

    @Test
    public void successfulMypage() throws Exception {
        // Setup Fixture
        String authorization = Utils.makeAuthorizationToken(userId);

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
        String authorization = Utils.makeExpiredAuthorizationToken(userId);

        // Exercise SUT
        ResultActions result = performMypage(authorization);

        // Verify Outcome
        String content = result.andReturn().getResponse().getContentAsString();
        assertEquals("", content);
    }

    private ResultActions performMypage(String authorization) throws Exception {
        URI uri = new URIBuilder("/api/mypage")
                .build();
        if(authorization == null) {
            return mockMvc.perform(MockMvcRequestBuilders.get(uri));
        } else {
            return mockMvc.perform(MockMvcRequestBuilders.get(uri)
                    .header("Authorization", authorization));
        }
    }

    @Test
    public void mypageUpdate() throws Exception {
        // Setup Fixture
        String authorization = Utils.makeAuthorizationToken(userId);
        String userName = "개명함";
        String userPhone = "01099991111";
        String userEmail = "ung27540421@outlook.com";
        String userAffiliation = "Korea";

        // Exercise SUT
        ResultActions result = performMypageUpdate(authorization, userName, userPhone, userEmail, userAffiliation);

        // Verify Outcome
        result.andExpect(header().string("update", "success"));
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("userName이 다르게 저장됨", userName, userEntity.getUserName());
            assertEquals("userPhone이 다르게 저장됨", userPhone, userEntity.getUserPhone());
            assertEquals("userEmail이 다르게 저장됨", userEmail, userEntity.getUserEmail());
            assertEquals("userAffiliation이 다르게 저장됨", userAffiliation, userEntity.getUserAffiliation());
        });
    }

    @Test
    public void mypageUpdateWithoutLogin() throws Exception {
        // Setup Fixture
        String authorization = null;
        String userName = "개명함";
        String userPhone = "01099991111";
        String userEmail = "ung27540421@outlook.com";
        String userAffiliation = "Korea";

        // Exercise SUT
        ResultActions result = performMypageUpdate(authorization, userName, userPhone, userEmail, userAffiliation);

        // Verify Outcome
        result.andExpect(header().string("update", "fail"));
    }

    @Test
    public void mypageUpdateWithExpiredAuthorizationToken() throws Exception {
        // Setup Fixture
        String authorization = Utils.makeExpiredAuthorizationToken(userId);
        String userName = "개명함";
        String userPhone = "01099991111";
        String userEmail = "ung27540421@outlook.com";
        String userAffiliation = "Korea";

        // Exercise SUT
        ResultActions result = performMypageUpdate(authorization, userName, userPhone, userEmail, userAffiliation);

        // Verify Outcome
        result.andExpect(header().string("update", "fail"));
    }

    private ResultActions performMypageUpdate(String authorization, String userName, String userPhone, String userEmail, String userAffiliation) throws Exception {
        URI uri = new URIBuilder("/api/mypage/update")
                .addParameter("userName", userName)
                .addParameter("userPhone", userPhone)
                .addParameter("userEmail", userEmail)
                .addParameter("userAffiliation", userAffiliation)
                .build();
        if(authorization == null) {
            return mockMvc.perform(MockMvcRequestBuilders.put(uri));
        } else {
            return mockMvc.perform(MockMvcRequestBuilders.put(uri)
                    .header("Authorization", authorization));
        }
    }

    @Test
    public void successfulExchangePoint() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        String authorization = Utils.makeAuthorizationToken(userId);
        int amount = 10000;
        int point = userEntityWrapper.get().getPoint();

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "success"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("userPoint가 잘못 계산됨", point - amount, userEntity.getPoint());
        });
    }

    @Test
    public void exchangePointWithoutRegisteringOpenBanking() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        String authorization = Utils.makeAuthorizationToken(userId);
        int amount = 10000;
        int point = userEntityWrapper.get().getPoint();
        userEntityWrapper.ifPresent(userEntity -> {
            userEntity.setUserOpenBankingAccessToken(UUID.randomUUID().toString().replace("-", ""));
            userEntity.setUserOpenBankingNum(0);
            userRepository.save(userEntity);
        });

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("변동되지 말아야 할 userPoint가 변동됨", point, userEntity.getPoint());
        });
    }

    @Test
    public void exchangePointMoreThanHaving() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        String authorization = Utils.makeAuthorizationToken(userId);
        int amount = 1000000;
        int point = userEntityWrapper.get().getPoint();

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("변동되지 말아야 할 userPoint가 변동됨", point, userEntity.getPoint());
        });
    }

    @Test
    public void exchangePointButOpenBankingError() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        String authorization = Utils.makeAuthorizationToken(userId);
        int amount = 10000;
        int point = userEntityWrapper.get().getPoint();
        userEntityWrapper.ifPresent(userEntity -> {
            userEntity.setUserOpenBankingAccessToken(Utils.makeExpiredAuthorizationToken(userId));
        });

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("변동되지 말아야 할 userPoint가 변동됨", point, userEntity.getPoint());
        });
    }

    @Test
    public void exchangePointWithoutLogin() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        String authorization = null;
        int amount = 10000;
        int point = userEntityWrapper.get().getPoint();
        userEntityWrapper.ifPresent(userEntity -> {
            userEntity.setUserOpenBankingAccessToken(Utils.makeExpiredAuthorizationToken(userId));
        });

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("변동되지 말아야 할 userPoint가 변동됨", point, userEntity.getPoint());
        });
    }

    @Test
    public void exchangePointWithExpiredAuthorizationToken() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        String authorization = Utils.makeExpiredAuthorizationToken(userId);
        int amount = 10000;
        int point = userEntityWrapper.get().getPoint();
        userEntityWrapper.ifPresent(userEntity -> {
            userEntity.setUserOpenBankingAccessToken(Utils.makeExpiredAuthorizationToken(userId));
        });

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, amount);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("변동되지 말아야 할 userPoint가 변동됨", point, userEntity.getPoint());
        });
    }

    @Test
    public void exchangePointWithoutAmount() throws Exception {
        // Setup Fixture
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        String authorization = null;
        int point = userEntityWrapper.get().getPoint();
        userEntityWrapper.ifPresent(userEntity -> {
            userEntity.setUserOpenBankingAccessToken(Utils.makeExpiredAuthorizationToken(userId));
        });

        // Exercise SUT
        ResultActions result = performExchangePoint(authorization, null);

        // Verify Outcome
        result.andExpect(header().string("exchange", "fail"));
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("변동되지 말아야 할 userPoint가 변동됨", point, userEntity.getPoint());
        });
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

    @Test
    public void top5RichUser() {
    }

    @Test
    public void top5SmartUser() {
    }
}