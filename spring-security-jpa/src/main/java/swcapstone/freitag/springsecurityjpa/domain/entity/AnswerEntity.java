package swcapstone.freitag.springsecurityjpa.domain.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity(name = "answer_table")
public class AnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id")
    private int problemId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "answer")
    private String answer;

    @Builder
    public AnswerEntity(Long id, int problemId, String userId, String answer) {
        this.id = id;
        this.problemId = problemId;
        this.userId = userId;
        this.answer = answer;
    }
}
