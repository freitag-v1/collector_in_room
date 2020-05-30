package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProblemDtoWithClassDto {

    private ProblemDto problemDto;
    private List<ClassDto> classNameList;

    public ProblemDtoWithClassDto(ProblemDto problemDto, List<ClassDto> classNameList) {
        this.problemDto = problemDto;
        this.classNameList = classNameList;
    }
}
