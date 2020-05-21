package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swcapstone.freitag.springsecurityjpa.domain.entity.ClassEntity;

@Getter
@NoArgsConstructor
public class ClassDto {

    // private Long id;
    private int projectId;
    private String className;

    public ClassEntity toEntity() {
        return ClassEntity.builder()
                //.id(id)
                .projectId(projectId)
                .className(className)
                .build();
    }

    @Builder
    public ClassDto(/*Long id, */int projectId, String className) {
        // this.id = id;
        this.projectId = projectId;
        this.className = className;
    }
}
