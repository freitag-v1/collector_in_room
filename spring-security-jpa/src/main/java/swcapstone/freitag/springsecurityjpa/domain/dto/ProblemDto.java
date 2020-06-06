package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

@Getter
@NoArgsConstructor
public class ProblemDto {

    private int problemId;
    private int projectId;
    private int referenceId;
    private String bucketName;
    private String objectName;
    private String answer;
    private String finalAnswer;
    private String validationStatus;
    private String userId;


    public ProblemEntity toEntity() {
        return ProblemEntity.builder()
                .problemId(problemId)
                .projectId(projectId)
                .referenceId(referenceId)
                .bucketName(bucketName)
                .objectName(objectName)
                .answer(answer)
                .finalAnswer(finalAnswer)
                .validationStatus(validationStatus)
                .userId(userId)
                .build();
    }


    @Builder
    public ProblemDto(int problemId, int projectId, int referenceId, String bucketName, String objectName,
                      String answer, String finalAnswer, String validationStatus, String userId) {
        this.problemId = problemId;
        this.projectId = projectId;
        this.referenceId = referenceId;
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.answer = answer;
        this.finalAnswer = finalAnswer;
        this.validationStatus = validationStatus;
        this.userId = userId;
    }
}
