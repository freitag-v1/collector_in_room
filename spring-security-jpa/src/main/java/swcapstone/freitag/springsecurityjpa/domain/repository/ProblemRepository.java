package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

    long count();
    long countByUserId(String userId);
    long countByUserIdAndValidationStatus(String userId, String validationStatus);
    long countByUserIdAndValidationStatusAndRightAnswer(String userId, String validationStatus, Boolean rightAnswer);
    Optional<ProblemEntity> findByProblemId(int problemId);
    Optional<ProblemEntity> findFirstByProjectIdAndValidationStatus(int projectId, String validationStatus);
    Optional<ProblemEntity> findTopByOrderByProblemIdDesc();
    List<ProblemEntity> findAllByProjectId(int projectId);
    List<ProblemEntity> findAllByReferenceIdAndValidationStatus(int referenceId, String validationStatus);
    List<ProblemEntity> findAllByProjectIdAndReferenceIdAndValidationStatus(int projectId, int referenceId, String validationStatus);
    @Transactional
    @Modifying
    @Query("delete from problem_table p where (p.projectId = :projectId) AND (p.validationStatus = '작업전' OR p.validationStatus = '교차검증전')")
    void deleteAllInTerminatedProject(@Param("projectId") int projectId);
}
