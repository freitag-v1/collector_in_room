package swcapstone.freitag.springsecurityjpa.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccuracyRankUserDto extends RankUserDto {
    @JsonProperty
    private double userAccuracy;

    public AccuracyRankUserDto(String userId, int numOfProblems, double userAccuracy) {
        super(userId, numOfProblems);
        this.userAccuracy = userAccuracy;
    }
}
