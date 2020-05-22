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
    private String objectId;
    private String finalAnswer;
    private String validationStatus;


    public ProblemEntity toEntity() {
        return ProblemEntity.builder()
                .problemId(problemId)
                .projectId(projectId)
                .referenceId(referenceId)
                .objectId(objectId)
                .finalAnswer(finalAnswer)
                .validationStatus(validationStatus)
                .build();
    }


    @Builder
    public ProblemDto(int problemId, int projectId, int referenceId, String objectId, String finalAnswer, String validationStatus) {
        this.problemId = problemId;
        this.projectId = projectId;
        this.referenceId = referenceId;
        this.objectId = objectId;
        this.finalAnswer = finalAnswer;
        this.validationStatus = validationStatus;
    }
}
