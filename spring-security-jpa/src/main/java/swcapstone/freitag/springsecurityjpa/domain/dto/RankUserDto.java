package swcapstone.freitag.springsecurityjpa.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RankUserDto {
    @JsonProperty
    private String userId;
    @JsonProperty
    private int numOfProblems;

    public RankUserDto(String userId, int numOfProblems) {
        this.userId = userId;
        this.numOfProblems = numOfProblems;
    }
}
