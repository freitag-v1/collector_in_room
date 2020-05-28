package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;

import javax.persistence.Column;
import java.sql.Timestamp;

// Data Transfer Object: SQL를 사용(개발자가 직접 코딩)하여 DB에 접근한 후 적절한 CRUD API를 제공
// DB에서 데이터를 얻어 Service나 Controller 등으터 보낼 때 사용하는 객체
// 즉, DB의 데이터가 Presentation Logic Tier로 넘어오게 될 때는 DTO의 모습으로 바껴서 오고가는 것
// 로직을 갖고 있지 않는 순수한 데이터 객체이며, getter/setter 메서드만
// 하지만 DB에서 꺼낸 값을 임의로 변경할 필요가 없기 때문에 DTO클래스에는 setter가 없다. (대신 생성자에서 값을 할당한다.)

// Entity Class와 DTO Class를 분리하는 이유?
// View Layer와 DB Layer의 역할을 철저하게 분리하기 위해서
// 테이블과 매핑되는 Entity 클래스가 변경되면 여러 클래스에 영향을 끼치게 되는
// 반면 View와 통신하는 DTO 클래스(Request / Response 클래스)는 자주 변경되므로 분리
// 즉 DTO는 Domain Model을 복사한 형태로, 다양한 Presentation Logic을 추가한 정도로 사용하며
// Domain Model 객체는 Persistent만을 위해서 사용

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDto {
    // private Long id;
    private String userId;
    private String userPassword;
    private String userName;
    private String userOpenBankingAccessToken;
    private int userOpenBankingNum;
    private String userPhone;
    private String userEmail;
    private String userAffiliation;
    private int userVisit;
    private Timestamp userLastVisit;
    private int totalPoint;
    private int point;

    public UserEntity toEntity() {  // toEntity() 메서드를 통해서 DTO에서 필요한 부분을 이용하여 Entity로 만든다.
        return UserEntity.builder()
                //.id(id)
                .userId(userId)
                .userPassword(userPassword)
                .userName(userName)
                .userOpenBankingAccessToken(userOpenBankingAccessToken)
                .userOpenBankingNum(userOpenBankingNum)
                .userPhone(userPhone)
                .userEmail(userEmail)
                .userAffiliation(userAffiliation)
                .userVisit(userVisit)
                .userLastVisit(userLastVisit)
                .totalPoint(totalPoint)
                .point(point)
                .build();
    }

    @Builder
    public UserDto(/*Long id, */String userId, String userPassword, String userName,
                                int userOpenBankingNum, String userOpenBankingAccessToken, String userPhone, String userEmail,
                                String userAffiliation, int userVisit, Timestamp userLastVisit, int totalPoint, int point) {
        // this.id = id;
        this.userId = userId;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userOpenBankingNum = userOpenBankingNum;
        this.userOpenBankingAccessToken = userOpenBankingAccessToken;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userAffiliation = userAffiliation;
        this.userVisit = userVisit;
        this.userLastVisit = userLastVisit;
        this.totalPoint = totalPoint;
        this.point = point;
    }
}
