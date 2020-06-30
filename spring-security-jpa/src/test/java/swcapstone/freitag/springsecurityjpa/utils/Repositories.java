package swcapstone.freitag.springsecurityjpa.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static swcapstone.freitag.springsecurityjpa.utils.Common.copyUserEntity;

@Service
public class Repositories {
    @Autowired
    private UserRepository userRepository;
    private Map<String, UserEntity> fixtureUserEntity = new HashMap<>();
    @Autowired
    private ProjectRepository projectRepository;

    public UserEntity getFixtureUserEntity(String userId) {
        if(!fixtureUserEntity.containsKey(userId)) {
            Optional<UserEntity> fixtureUserEntityOptional = userRepository.findByUserId(userId);
            assertTrue(fixtureUserEntityOptional.isPresent());
            fixtureUserEntity.put(userId, copyUserEntity(fixtureUserEntityOptional.get()));
        }
        return copyUserEntity(fixtureUserEntity.get(userId));
    }

    public UserEntity getUserEntity(String userId) {
        Optional<UserEntity> UserEntityOptional = userRepository.findByUserId(userId);
        assertTrue(UserEntityOptional.isPresent());
        return UserEntityOptional.get();
    }

    public void saveUserEntity(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    public ProjectEntity getProjectEntity(int projectId) {
        Optional<ProjectEntity> ProejctEntityOptional = projectRepository.findByProjectId(projectId);
        assertTrue(ProejctEntityOptional.isPresent());
        return ProejctEntityOptional.get();
    }
}
