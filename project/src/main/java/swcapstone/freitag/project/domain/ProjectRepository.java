package swcapstone.freitag.project.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByUserId(String userId);
    List<ProjectEntity> findAllByUserId(String userId);
}
