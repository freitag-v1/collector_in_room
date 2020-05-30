package swcapstone.freitag.springsecurityjpa.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProjectDtoWithClassDto {

    @JsonProperty
    private ProjectDto projectDto;
    @JsonProperty
    private List<ClassDto> classNameList;

    public ProjectDtoWithClassDto(ProjectDto projectDto, List<ClassDto> classNameList) {
        this.projectDto = projectDto;
        this.classNameList = classNameList;
    }
}
