package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swcapstone.freitag.springsecurityjpa.domain.entity.LabellingWorkHistoryEntity;

public interface LabellingWorkHistoryRepository extends JpaRepository<LabellingWorkHistoryEntity, Long> {
    long count();
}
