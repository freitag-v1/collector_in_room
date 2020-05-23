package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swcapstone.freitag.springsecurityjpa.domain.entity.AnswerEntity;

@Getter
@NoArgsConstructor
public class AnswerDto {
    private int problemId;
    private String userId;
    private String answer;

    public AnswerEntity toEntity() {
        return AnswerEntity.builder()
                .problemId(problemId)
                .userId(userId)
                .answer(answer)
                .build();
    }

    @Builder
    public AnswerDto(int problemId, String userId, String answer) {
        this.problemId = problemId;
        this.userId = userId;
        this.answer = answer;
    }

}
