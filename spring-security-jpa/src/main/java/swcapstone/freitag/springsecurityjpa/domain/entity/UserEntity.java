package swcapstone.freitag.springsecurityjpa.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

// Entity Class: 실제 DB의 테이블과 매칭될 클래스
// Domain Logic만 가지고 있어야 하고 Presentation Logic을 가지고 있어서는 안됨
// 여기서 구현한 method는 주로 Service Layer에서 사용

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
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

    @Column(name = "user_openbanking_access_token")
    private String userOpenBankingAccessToken;

    @Column(name = "user_openbanking_num")
    private int userOpenBankingNum;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_affiliation")
    private String userAffiliation;

    @Column(name = "user_visit")
    private int userVisit;

    @Column(name = "user_last_visit")
    private Timestamp userLastVisit;

    @Column(name = "user_total_point")
    private int totalPoint;

    @Column(name = "user_point")
    private int point;

    @Builder
    public UserEntity(Long id, String userId, String userPassword, String userName,
                      int userOpenBankingNum, String userOpenBankingAccessToken, String userPhone, String userEmail,
                      String userAffiliation, int userVisit, Timestamp userLastVisit, int totalPoint, int point) {
        this.id = id;
        this.userId = userId;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userOpenBankingAccessToken = userOpenBankingAccessToken;
        this.userOpenBankingNum = userOpenBankingNum;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userAffiliation = userAffiliation;
        this.userVisit = userVisit;
        this.userLastVisit = userLastVisit;
        this.totalPoint = totalPoint;
        this.point = point;
    }
}