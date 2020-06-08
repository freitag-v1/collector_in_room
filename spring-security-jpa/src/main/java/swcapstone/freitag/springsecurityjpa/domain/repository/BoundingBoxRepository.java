package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swcapstone.freitag.springsecurityjpa.domain.entity.BoundingBoxEntity;

import java.util.List;

@Repository
public interface BoundingBoxRepository extends JpaRepository<BoundingBoxEntity, Long> {
    List<BoundingBoxEntity> findAllByProblemIdAndClassName(int problemId, String className);
}
