package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swcapstone.freitag.springsecurityjpa.domain.entity.CollectionWorkHistoryEntity;

@Getter
@NoArgsConstructor
public class CollectionWorkHistoryDto {
    private int problemId;
    private String userId;

    public CollectionWorkHistoryEntity toEntity() {
        return CollectionWorkHistoryEntity.builder()
                .problemId(problemId)
                .userId(userId)
                .build();
    }

    @Builder
    public CollectionWorkHistoryDto(int problemId, String userId) {
        this.problemId = problemId;
        this.userId = userId;
    }
}


