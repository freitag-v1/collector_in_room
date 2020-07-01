package swcapstone.freitag.springsecurityjpa.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swcapstone.freitag.springsecurityjpa.domain.entity.ClassEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ClassRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProblemRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static swcapstone.freitag.springsecurityjpa.utils.Fixture.copyUserEntity;

@Service
public class Repositories {
    @Autowired
    private UserRepository userRepository;
    private Map<String, UserEntity> fixtureUserEntity = new HashMap<>();
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private ProblemRepository problemRepository;

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

    public Map<Integer, ProjectEntity> getProjectEntityList(String userId) {
        Map<Integer, ProjectEntity> projectEntityList = new HashMap<>();
        List<ProjectEntity> fixtureProjectEntityList = projectRepository.findAllByUserId(userId);
        for (ProjectEntity projectEntity : fixtureProjectEntityList) {
            projectEntityList.put(projectEntity.getProjectId(), projectEntity);
        }
        return projectEntityList;
    }

    public void saveProjectEntity(ProjectEntity projectEntity) {
        projectRepository.save(projectEntity);
    }

    public void deletaAllProject() {
        projectRepository.deleteAllInBatch();
    }

    public List<ClassEntity> getClassEntityList(int projectId) {
        return classRepository.findAllByProjectId(projectId);
    }

    public void deletaAllClass() {
        classRepository.deleteAllInBatch();
    }

    public List<ProblemEntity> getProblemEntityList(int projectId) {
        return problemRepository.findAllByProjectId(projectId);
    }

    public void deletaAllProblem() {
        problemRepository.deleteAllInBatch();
    }

    public void saveProblemEntity(ProblemEntity fixtureProblemEntity) {
        problemRepository.save(fixtureProblemEntity);
    }
}
