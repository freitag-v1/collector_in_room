package swcapstone.freitag.springsecurityjpa.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TotalPointRankUserDto extends RankUserDto {
    @JsonProperty
    private int totalPoint;

    public TotalPointRankUserDto(String userId, int numOfProblems, int totalPoint) {
        super(userId, numOfProblems);
        this.totalPoint = totalPoint;
    }
}
