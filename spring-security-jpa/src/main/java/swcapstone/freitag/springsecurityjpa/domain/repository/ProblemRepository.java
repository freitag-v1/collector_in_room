package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

    long count();
    long countByUserIdAAndValidationStatus(String userId, String validationStatus);
    Optional<ProblemEntity> findByProblemId(int problemId);
    Optional<ProblemEntity> findFirstByProjectIdAndValidationStatus(int projectId, String validationStatus);
    Optional<ProblemEntity> findTopByOrderByIdDesc();
    List<ProblemEntity> findAllByProjectId(int projectId);
    List<ProblemEntity> findAllByReferenceIdAndValidationStatus(int referenceId, String validationStatus);

    @Query(value = "SELECT COUNT(*) FROM problem_table " +
            "WHERE userId=:userId and validationStatus=:validationStatus and answer=finalAnswer")
    long countRightProblems(String userId, String validationStatus);
}
