package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swcapstone.freitag.springsecurityjpa.domain.entity.AnswerEntity;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {

}
