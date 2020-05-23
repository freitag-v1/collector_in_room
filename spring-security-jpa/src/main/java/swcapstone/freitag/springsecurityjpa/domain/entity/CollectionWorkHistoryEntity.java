package swcapstone.freitag.springsecurityjpa.domain.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity(name = "collection_work_history")
public class CollectionWorkHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id")
    private int problemId;

    @Column(name = "user_id")
    private String userId;

    @Builder
    public CollectionWorkHistoryEntity(Long id, int problemId, String userId) {
        this.id = id;
        this.problemId = problemId;
        this.userId = userId;
    }

}
