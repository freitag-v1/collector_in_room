package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swcapstone.freitag.springsecurityjpa.domain.entity.BoundingBoxEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoundingBoxRepository extends JpaRepository<BoundingBoxEntity, Long> {
    Optional<BoundingBoxEntity> findTopByOrderByIdDesc();
    List<BoundingBoxEntity> findAllByProblemId(int problemId);
}
