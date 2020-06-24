package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Getter;

public abstract class RankUserDto {

    private String userId;
    private int numOfProblems;

    public RankUserDto(String userId, int numOfProblems) {
        this.userId = userId;
        this.numOfProblems = numOfProblems;
    }
}
