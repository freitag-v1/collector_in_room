package swcapstone.freitag.springsecurityjpa.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

// Entity Class: 실제 DB의 테이블과 매칭될 클래스
// Domain Logic만 가지고 있어야 하고 Presentation Logic을 가지고 있어서는 안됨
// 여기서 구현한 method는 주로 Service Layer에서 사용

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity(name = "user_table")
public class UserEntity {
    @Id // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_pw")
    private String userPassword;

    @Column(name = "user_name")
    private String userName;

    @Builder
    public UserEntity(Long id, String userId, String userPassword, String userName) {
        this.id = id;
        this.userId = userId;
        this.userPassword = userPassword;
        this.userName = userName;
    }
}