package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Getter;

@Getter
public class RankUserDto {

    private String userId;
    private int totalPoint;

    public RankUserDto(String userId, int totalPoint) {
        this.userId = userId;
        this.totalPoint = totalPoint;
    }
}
