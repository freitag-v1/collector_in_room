package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swcapstone.freitag.springsecurityjpa.domain.entity.CollectionWorkHistoryEntity;

public interface CollectionWorkHistoryRepository extends JpaRepository<CollectionWorkHistoryEntity, Long> {
}
