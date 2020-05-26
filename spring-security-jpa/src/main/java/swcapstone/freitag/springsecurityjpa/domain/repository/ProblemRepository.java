package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

    long count();
    Optional<ProblemEntity> findByProblemId(int problemId);
    Optional<ProblemEntity> findFirstByProjectIdAndObjectName(int projectId, String objectName);
    List<ProblemEntity> findAllByProjectIdAndValidationStatus(int projectId, String validationStatus);
}
