package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkHistoryDto {

    private String projectRequester;    // 작업자가 참여한 프로젝트의 의뢰자 이름
    private String projectName;         // 작업자가 참여한 프로젝트의 이름
    private String projectWorkType;     // 작업자가 참여한 프로젝트의 종류(수집/라벨링)
    private String projectDataType;     // 작업자가 참여한 프로젝트의 데이터 종류(이미지, 음성, 텍스트) 혹은 라벨링 종류(분류, 바운딩박스)
    private String projectStatus;       // 작업자가 참여한 프로젝트의 진행 정도
    private int problemId;              // 작업자가 작업한 문제의 번호

    public WorkHistoryDto(String projectRequester, String projectName, String projectWorkType,
                          String projectDataType, String projectStatus, int problemId) {

        this.projectRequester = projectRequester;
        this.projectName = projectName;
        this.projectWorkType = projectWorkType;
        this.projectDataType = projectDataType;
        this.projectStatus = projectStatus;
        this.problemId = problemId;
    }

}
