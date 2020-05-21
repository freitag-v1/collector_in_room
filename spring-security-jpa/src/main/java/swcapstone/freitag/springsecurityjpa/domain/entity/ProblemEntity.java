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
    @Column(name = "problem_id")
    private int problemId;

    @Column(name = "project_id")
    private int projectId;

    @Column(name = "reference_id")
    private int referenceId;

    @Column(name = "object_id")
    private String objectId;

    @Column(name = "final_answer")
    private String finalAnswer;

    @Column(name = "validation_status")
    private String validationStatus;

    @Builder
    public ProblemEntity(int problemId, int projectId, int referenceId, String objectId, String finalAnswer, String validationStatus) {
        this.problemId = problemId;
        this.projectId = projectId;
        this.referenceId = referenceId;
        this.objectId = objectId;
        this.finalAnswer = finalAnswer;
        this.validationStatus = validationStatus;
    }
}
