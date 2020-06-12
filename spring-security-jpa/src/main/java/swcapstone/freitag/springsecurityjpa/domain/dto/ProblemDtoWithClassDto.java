package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ProblemDtoWithClassDto {

    private ProblemDto problemDto;
    private List<ClassDto> classNameList;
    private List<BoundingBoxDto> boundingBoxList;
    private String conditionContent;

    public ProblemDtoWithClassDto(ProblemDto problemDto, List<ClassDto> classNameList, String conditionContent) {
        this.problemDto = problemDto;
        this.classNameList = classNameList;
        this.conditionContent = conditionContent;
    }

    public void setBoundingBoxList(List<BoundingBoxDto> boundingBoxList) {
        this.boundingBoxList = boundingBoxList;
    }
}
