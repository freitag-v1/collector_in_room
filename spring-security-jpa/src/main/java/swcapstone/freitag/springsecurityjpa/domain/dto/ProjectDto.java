package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Builder;
import lombok.NoArgsConstructor;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

@NoArgsConstructor
public class ProjectDto {

    private String userId;
    private String projectName;
    private String bucketName;  // 의뢰자가 업로드하는 라벨링 데이터를 담을 버킷
    private String status;  // 없음, 진행중, 완료
    private String workType;
    private String dataType;
    private String subject;
    private int difficulty;  // 0~5점
    private String wayContent;  // 작업 방법
    private String conditionContent;    // 작업 조건
    private String exampleContent;
    private String description; // 프로젝트 설명
    private int totalData;
    private int progressData;
    private int cost;

    public ProjectEntity toEntity() {
        return ProjectEntity.builder()
                .userId(userId)
                .projectName(projectName)
                .bucketName(bucketName)
                .status(status)
                .workType(workType)
                .dataType(dataType)
                .subject(subject)
                .difficulty(difficulty)
                .wayContent(wayContent)
                .conditionContent(conditionContent)
                .exampleContent(exampleContent)
                .description(description)
                .totalData(totalData)
                .progressData(progressData)
                .cost(cost)
                .build();
    }

    @Builder
    public ProjectDto(String userId, String projectName, String bucketName, String status,
                      String workType, String dataType, String subject, int difficulty, String wayContent,
                      String conditionContent, String exampleContent, String description, int totalData, int progressData, int cost) {

        this.userId = userId;
        this.projectName = projectName;
        this.bucketName = bucketName;
        this.status = status;
        this.workType = workType;
        this.dataType = dataType;
        this.subject = subject;
        this.difficulty = difficulty;
        this.wayContent = wayContent;
        this.conditionContent = conditionContent;
        this.exampleContent = exampleContent;
        this.description = description;
        this.totalData = totalData;
        this.progressData = progressData;
        this.cost = cost;
    }
}

