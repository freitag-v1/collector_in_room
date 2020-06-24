package swcapstone.freitag.springsecurityjpa.domain.dto;

public class TotalPointRankUserDto extends RankUserDto {
    private int totalPoint;


    public TotalPointRankUserDto(String userId, int numOfProblems, int totalPoint) {
        super(userId, numOfProblems);
        this.totalPoint = totalPoint;
    }
}
