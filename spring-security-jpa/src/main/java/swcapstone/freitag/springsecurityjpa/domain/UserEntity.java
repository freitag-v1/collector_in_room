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
    @Id // Primary Key - JPA는 이 id를 통해 객체를 구분
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_pw")
    private String userPassword;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_bank")
    private int userBank;

    @Column(name = "user_account")
    private String userAccount;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_affiliation")
    private String userAffiliation;

    @Column(name = "user_visit")
    private int userVisit;

    @Column(name = "user_total_point")
    private int totalPoint;

    @Column(name = "user_point")
    private int point;

    @Builder
    public UserEntity(Long id, String userId, String userPassword, String userName,
                       int userBank, String userAccount, String userPhone, String userEmail,
                        String userAffiliation, int userVisit, int totalPoint, int point) {
        this.id = id;
        this.userId = userId;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userBank = userBank;
        this.userAccount = userAccount;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userAffiliation = userAffiliation;
        this.userVisit = userVisit;
        this.totalPoint = totalPoint;
        this.point = point;
    }
}