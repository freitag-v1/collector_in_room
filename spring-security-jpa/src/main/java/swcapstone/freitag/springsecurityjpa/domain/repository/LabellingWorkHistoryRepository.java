package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swcapstone.freitag.springsecurityjpa.domain.entity.LabellingWorkHistoryEntity;

import java.util.List;
import java.util.Optional;

public interface LabellingWorkHistoryRepository extends JpaRepository<LabellingWorkHistoryEntity, Long> {

    long count();
    void deleteByHistoryId(int historyId);
    Optional<LabellingWorkHistoryEntity> findTopByOrderByIdDesc();
    Optional<LabellingWorkHistoryEntity> findByHistoryId(int historyId);
    List<LabellingWorkHistoryEntity> findAllByUserId(String userId);

}
