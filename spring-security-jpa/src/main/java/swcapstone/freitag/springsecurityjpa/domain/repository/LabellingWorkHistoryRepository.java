package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swcapstone.freitag.springsecurityjpa.domain.entity.LabellingWorkHistoryEntity;

import java.util.Optional;

public interface LabellingWorkHistoryRepository extends JpaRepository<LabellingWorkHistoryEntity, Long> {

    long count();
    void deleteByHistoryId(int historyId);
    Optional<LabellingWorkHistoryEntity> findByHistoryId(int historyId);
}
