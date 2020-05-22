package swcapstone.freitag.springsecurityjpa.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
