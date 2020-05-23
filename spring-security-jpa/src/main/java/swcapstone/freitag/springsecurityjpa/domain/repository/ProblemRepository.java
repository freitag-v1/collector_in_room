package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

    Optional<ProblemEntity> findFirstByProjectIdAndObjectId(int projectId, String objectId);
}
