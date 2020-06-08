package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swcapstone.freitag.springsecurityjpa.domain.entity.BoundingBoxEntity;


@Getter
@NoArgsConstructor
public class BoundingBoxDto {
    private int problemId;
    private String className;
    private String coordinates;

    public BoundingBoxEntity toEntity() {
        return BoundingBoxEntity.builder()
                .problemId(problemId)
                .className(className)
                .coordinates(coordinates)
                .build();
    }

    @Builder
    public BoundingBoxDto(int problemId, String className, String coordinates) {
        this.problemId = problemId;
        this.className = className;
        this.coordinates = coordinates;
    }
}
