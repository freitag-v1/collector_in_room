package swcapstone.freitag.springsecurityjpa.domain.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity(name = "labelling_work_history")
public class LabellingWorkHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "history_id")
    private int historyId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "uv1")
    private int uv1;

    @Column(name = "cv1")
    private int cv1;

    @Column(name = "cv2")
    private int cv2;

    @Column(name = "lp1")
    private int lp1;

    @Column(name = "lp2")
    private int lp2;



    @Builder
    public LabellingWorkHistoryEntity(Long id, int historyId, String userId, String dataType,
                                      int uv1, int cv1, int cv2, int lp1, int lp2) {

        this.id = id;
        this.historyId = historyId;
        this.userId = userId;
        this.dataType = dataType;

        this.uv1 = uv1;
        this.cv1 = cv1;
        this.cv2 = cv2;
        this.lp1 = lp1;
        this.lp2 = lp2;

    }
}
