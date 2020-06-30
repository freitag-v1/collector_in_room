package swcapstone.freitag.springsecurityjpa.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Service
public class Repositories {
    @Autowired
    private UserRepository userRepository;

    public UserEntity getUserEntity(String userId) {
        Optional<UserEntity> fixtureUserEntityOptional = userRepository.findByUserId(userId);
        assertTrue(fixtureUserEntityOptional.isPresent());
        return fixtureUserEntityOptional.get();
    }

    public void saveUserEntity(UserEntity userEntity) {
        userRepository.save(userEntity);
    }
}
