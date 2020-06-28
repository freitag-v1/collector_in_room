package swcapstone.freitag.springsecurityjpa.controller;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.utils.Utils;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private final String baseURI = "/api/login";
    private final String userId = "some_user";
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
    public void loginAndGetReward() throws Exception {
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
        URI uri = new URIBuilder(baseURI)
                .setParameter("userId", userId)
                .setParameter("userPassword", userPassword)
                .build();
        return mockMvc.perform(MockMvcRequestBuilders.post(uri));
    }

    @Test
    public void successfulSignUp() {
    }

    @Test
    public void signUpWithUserIdInUse() {
    }

    @Test
    public void successfulMypage() {
    }

    @Test
    public void mypageWithoutLogin() {
    }

    @Test
    public void mypageUpdate() {
    }

    @Test
    public void successfulExchangePoint() {
    }

    @Test
    public void exchangePointWithoutRegisteringOpenBanking() {
    }

    @Test
    public void exchangePointImpossible() {
    }

    @Test
    public void exchangePointWithoutLogin() {
    }

    @Test
    public void top5RichUser() {
    }

    @Test
    public void top5SmartUser() {
    }
}