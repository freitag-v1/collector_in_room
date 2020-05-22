package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    // 정적 쿼리
    long count();
    List<ProjectEntity> findAllByUserId(String userId);
    Optional<ProjectEntity> findByUserIdAndStatus(String userId, String status);
    Optional<ProjectEntity> findByProjectId(int projectId);
    Optional<ProjectEntity> findByBucketName(String bucketName);
}

