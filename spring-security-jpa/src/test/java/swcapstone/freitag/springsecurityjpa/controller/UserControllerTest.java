package swcapstone.freitag.springsecurityjpa.controller;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import swcapstone.freitag.springsecurityjpa.utils.Utils;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void successfulLogin() {
        String baseURI = "/api/login";
        String userId = "normal_user";
        String userPassword = Utils.SHA256("freitag123!");

        // Exercise SUT
        webTestClient.post().uri(uriBuilder ->
            uriBuilder.path(baseURI)
                    .queryParam("userId", userId)
                    .queryParam("userPassword", userPassword)
                    .build())
                .exchange()

                // Verify Outcome
                .expectHeader().exists("Authorization");
    }

    @Test
    public void loginWithNotSignedUpId() {
        String baseURI = "/api/login";
        String userId = "not_signed_up_user";
        String userPassword = Utils.SHA256("freitag123!");

        // Exercise SUT
        webTestClient.post().uri(uriBuilder ->
                uriBuilder.path(baseURI)
                        .queryParam("userId", userId)
                        .queryParam("userPassword", userPassword)
                        .build())
                .exchange()

                // Verify Outcome
                .expectHeader().doesNotExist("Authorization");
    }

    @Test
    public void loginWithWrongPassword() {
        String baseURI = "/api/login";
        String userId = "normal_user";
        String userPassword = Utils.SHA256("1234");

        // Exercise SUT
        webTestClient.post().uri(uriBuilder ->
                uriBuilder.path(baseURI)
                        .queryParam("userId", userId)
                        .queryParam("userPassword", userPassword)
                        .build())
                .exchange()
                .expectHeader().doesNotExist("Authorization");
    }

    @Test
    public void loginAndGetReward() {
        String baseURI = "/api/login";
        String userId = "newbie_user";
        String userPassword = Utils.SHA256("freitag123!");

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
        webTestClient.post().uri(uriBuilder ->
                uriBuilder.path(baseURI)
                        .queryParam("userId", userId)
                        .queryParam("userPassword", userPassword)
                        .build())
                .exchange()

                // Verify Outcome
                .expectHeader().exists("Authorization")
                .expectHeader().valueEquals("reward", "true");
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("총 획득 포인트가 제대로 변경되지 않음", totalPoint + 100, userEntity.getTotalPoint());
            assertEquals("현재 포인트가 제대로 변경되지 않음", point + 100, userEntity.getPoint());
            assertEquals("방문 일수가 제대로 변경되지 않음", 1, userEntity.getUserVisit());
            assertNotEquals("마지막 방문일이 제대로 변경되지 않음", new Timestamp(0), userEntity.getUserLastVisit());
        });
    }

    @Test
    public void loginButNotGetReward() {
        String baseURI = "/api/login";
        String userId = "oldbie_user";
        String userPassword = Utils.SHA256("freitag123!");

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
        webTestClient.post().uri(uriBuilder ->
                uriBuilder.path(baseURI)
                        .queryParam("userId", userId)
                        .queryParam("userPassword", userPassword)
                        .build())
                .exchange()

                // Verify Outcome
                .expectHeader().exists("Authorization");
        userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(userEntity -> {
            assertEquals("총 획득 포인트가 변경됨", totalPoint, userEntity.getTotalPoint());
            assertEquals("현재 포인트가 변경됨", point, userEntity.getPoint());
        });
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