package swcapstone.freitag.springsecurityjpa.domain.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity(name = "problem_table")
public class ProblemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "problem_id")
    private int problemId;

    @Column(name = "project_id")
    private int projectId;

    @Column(name = "reference_id")
    private int referenceId;

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "answer")
    private String answer;

    @Column(name = "final_answer")
    private String finalAnswer;

    @Column(name = "validation_status")
    private String validationStatus;    // 작업전, 작업후, 검증완료

    @Column(name = "user_id")
    private String userId;

    @Column(name = "level")
    private String level;

    @Column(name = "right_answer")
    private boolean rightAnswer;

    public boolean getRightAnswer() {
        return this.rightAnswer;
    }

    @Builder
    public ProblemEntity(int problemId, int projectId, int referenceId, String bucketName, String objectName,
                         String answer, String finalAnswer, String validationStatus, String userId, String level) {
        this.problemId = problemId;
        this.projectId = projectId;
        this.referenceId = referenceId;
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.answer = answer;
        this.finalAnswer = finalAnswer;
        this.validationStatus = validationStatus;
        this.userId = userId;
        this.level = level;
    }
}
