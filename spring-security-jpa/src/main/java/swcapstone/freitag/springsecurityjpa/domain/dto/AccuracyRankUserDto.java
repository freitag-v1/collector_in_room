package swcapstone.freitag.springsecurityjpa.domain.dto;

public class AccuracyRankUserDto extends RankUserDto {
    private double userAccuracy;

    public AccuracyRankUserDto(String userId, int numOfProblems, double userAccuracy) {
        super(userId, numOfProblems);
        this.userAccuracy = userAccuracy;
    }
}
