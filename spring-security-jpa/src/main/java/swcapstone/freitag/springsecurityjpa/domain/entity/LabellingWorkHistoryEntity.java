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

    @Column(name = "uv2")
    private int uv2;

    @Column(name = "uv3")
    private int uv3;

    @Column(name = "uv4")
    private int uv4;

    @Column(name = "uv5")
    private int uv5;

    @Column(name = "uv6")
    private int uv6;

    @Column(name = "uv7")
    private int uv7;

    @Column(name = "uv8")
    private int uv8;

    @Column(name = "uv9")
    private int uv9;

    @Column(name = "uv10")
    private int uv10;

    @Column(name = "cv1")
    private int cv1;

    @Column(name = "cv2")
    private int cv2;

    @Column(name = "cv3")
    private int cv3;

    @Column(name = "cv4")
    private int cv4;

    @Column(name = "cv5")
    private int cv5;

    @Column(name = "cv6")
    private int cv6;

    @Column(name = "cv7")
    private int cv7;

    @Column(name = "cv8")
    private int cv8;

    @Column(name = "cv9")
    private int cv9;

    @Column(name = "cv10")
    private int cv10;

    @Column(name = "cv11")
    private int cv11;

    @Column(name = "cv12")
    private int cv12;

    @Column(name = "cv13")
    private int cv13;

    @Column(name = "cv14")
    private int cv14;

    @Column(name = "cv15")
    private int cv15;

    @Column(name = "cv16")
    private int cv16;

    @Column(name = "cv17")
    private int cv17;

    @Column(name = "cv18")
    private int cv18;

    @Column(name = "cv19")
    private int cv19;

    @Column(name = "cv20")
    private int cv20;

    @Column(name = "lp1")
    private int lp1;

    @Column(name = "lp2")
    private int lp2;

    @Column(name = "lp3")
    private int lp3;

    @Column(name = "lp4")
    private int lp4;

    @Column(name = "lp5")
    private int lp5;

    @Column(name = "lp6")
    private int lp6;

    @Column(name = "lp7")
    private int lp7;

    @Column(name = "lp8")
    private int lp8;

    @Column(name = "lp9")
    private int lp9;

    @Column(name = "lp10")
    private int lp10;

    @Column(name = "lp11")
    private int lp11;

    @Column(name = "lp12")
    private int lp12;

    @Column(name = "lp13")
    private int lp13;

    @Column(name = "lp14")
    private int lp14;

    @Column(name = "lp15")
    private int lp15;

    @Column(name = "lp16")
    private int lp16;

    @Column(name = "lp17")
    private int lp17;

    @Column(name = "lp18")
    private int lp18;

    @Column(name = "lp19")
    private int lp19;

    @Column(name = "lp20")
    private int lp20;


    @Builder
    public LabellingWorkHistoryEntity(Long id, int historyId, String userId, String dataType,
                                      int uv1, int uv2, int uv3, int uv4, int uv5, int uv6, int uv7, int uv8, int uv9, int uv10,
                                      int cv1, int cv2, int cv3, int cv4, int cv5, int cv6, int cv7, int cv8, int cv9, int cv10,
                                      int cv11, int cv12, int cv13, int cv14, int cv15, int cv16, int cv17, int cv18, int cv19, int cv20,
                                      int lp1, int lp2, int lp3, int lp4, int lp5, int lp6, int lp7, int lp8, int lp9, int lp10,
                                      int lp11, int lp12, int lp13, int lp14, int lp15, int lp16, int lp17, int lp18, int lp19, int lp20) {

        this.id = id;
        this.historyId = historyId;
        this.userId = userId;
        this.dataType = dataType;

        this.uv1 = uv1;
        this.uv2 = uv2;
        this.uv3 = uv3;
        this.uv4 = uv4;
        this.uv5 = uv5;
        this.uv6 = uv6;
        this.uv7 = uv7;
        this.uv8 = uv8;
        this.uv9 = uv9;
        this.uv10 = uv10;

        this.cv1 = cv1;
        this.cv2 = cv2;
        this.cv3 = cv3;
        this.cv4 = cv4;
        this.cv5 = cv5;
        this.cv6 = cv6;
        this.cv7 = cv7;
        this.cv8 = cv8;
        this.cv9 = cv9;
        this.cv10 = cv10;
        this.cv11 = cv11;
        this.cv12 = cv12;
        this.cv13 = cv13;
        this.cv14 = cv14;
        this.cv15 = cv15;
        this.cv16 = cv16;
        this.cv17 = cv17;
        this.cv18 = cv18;
        this.cv19 = cv19;
        this.cv20 = cv20;

        this.lp1 = lp1;
        this.lp2 = lp2;
        this.lp3 = lp3;
        this.lp4 = lp4;
        this.lp5 = lp5;
        this.lp6 = lp6;
        this.lp7 = lp7;
        this.lp8 = lp8;
        this.lp9 = lp9;
        this.lp10 = lp10;
        this.lp11 = lp11;
        this.lp12 = lp12;
        this.lp13 = lp13;
        this.lp14 = lp14;
        this.lp15 = lp15;
        this.lp16 = lp16;
        this.lp17 = lp17;
        this.lp18 = lp18;
        this.lp19 = lp19;
        this.lp20 = lp20;
    }
}
