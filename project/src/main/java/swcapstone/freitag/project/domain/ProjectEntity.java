package swcapstone.freitag.project.domain;

import lombok.*;

import javax.persistence.*;

// Entity Class: 실제 DB의 테이블과 매칭될 클래스
// Domain Logic만 가지고 있어야 하고 Presentation Logic을 가지고 있어서는 안됨
// 여기서 구현한 method는 주로 Service Layer에서 사용

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity(name = "project_table")
public class ProjectEntity {
    @Id // Primary Key - JPA는 이 id를 통해 객체를 구분
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int projectId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "bucket_name")
    private String bucketName;  // 의뢰자가 업로드하는 라벨링 데이터를 담을 버킷

    @Column(name = "status")
    private String status;  // 없음, 진행중, 완료

    @Column(name = "work_type")
    private String workType;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "subject")
    private String subject;

    @Column(name = "difficulty")
    private int difficulty;  // 0~5점

    @Column(name = "way_content")
    private String wayContent;  // 작업 방법

    @Column(name = "condition_content")
    private String conditionContent;    // 작업 조건

    @Column(name = "example_content")
    private String exampleContent;         // 예시 데이터의 object Etag

    @Column(name = "description")
    private String description; // 프로젝트 설명

    @Column(name = "total_data")
    private int totalData;

    @Column(name = "progress_data")
    private int progressData;

    @Column(name = "cost")
    private int cost;

    @Builder
    public ProjectEntity(int projectId, String userId, String projectName, String bucketName, String status,
                         String workType, String dataType, String subject, int difficulty, String wayContent,
                         String conditionContent, String exampleContent, String description, int totalData, int progressData, int cost) {

        this.projectId = projectId;
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
