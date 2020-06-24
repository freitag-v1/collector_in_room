package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;

import java.sql.Timestamp;

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
    private double userAccuracy;

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
                .userAccuracy(userAccuracy)
                .build();
    }

    @Builder
    public UserDto(/*Long id, */String userId, String userPassword, String userName,
                                int userOpenBankingNum, String userOpenBankingAccessToken, String userPhone, String userEmail,
                                String userAffiliation, int userVisit, Timestamp userLastVisit, int totalPoint, int point, double userAccuracy) {
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
        this.userAccuracy = userAccuracy;
    }
}
