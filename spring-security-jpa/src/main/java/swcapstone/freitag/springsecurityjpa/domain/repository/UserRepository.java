package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;

import java.util.List;
import java.util.Optional;

// Data Access Object: 실제로 DB에 접근하는 객체
// Service와 DB를 연결하는 고리의 역할
// SQL를 사용(개발자가 직접 코딩)하여 DB에 접근한 후 적절한 CRUD API를 제공

// Repository(DAO) <-- domain(entity) --> DB

public interface UserRepository extends JpaRepository<UserEntity, Long> {   // JPA 대부분의 기본적인 CRUD method를 제공
    // userId를 Where 조건절로 하여, 데이터를 가져올 수 있도록 findByUserId() 메서드를 정의
    // findBy에 이어 해당 Entity 필드 이름을 입력하면 검색 쿼리를 실행한 결과를 전달
    // SQL의 where절을 메서드 이름을 통해 전달한다고 생각하면 됨
    Optional<UserEntity> findByUserId(String userId);
    Optional<UserEntity> findByUserOpenBankingAccessToken(String userOpenBankingAccessToken);
    List<UserEntity> findTop3ByOrderByTotalPointDesc();
}