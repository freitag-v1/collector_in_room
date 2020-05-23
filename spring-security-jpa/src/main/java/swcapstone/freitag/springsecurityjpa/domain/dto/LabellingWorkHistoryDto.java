package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swcapstone.freitag.springsecurityjpa.domain.entity.LabellingWorkHistoryEntity;

@Getter
@NoArgsConstructor
public class LabellingWorkHistoryDto {

    private int historyId;
    private String userId;
    private String dataType;

    private int uv1;
    private int uv2;
    private int uv3;
    private int uv4;
    private int uv5;
    private int uv6;
    private int uv7;
    private int uv8;
    private int uv9;
    private int uv10;

    private int cv1;
    private int cv2;
    private int cv3;
    private int cv4;
    private int cv5;
    private int cv6;
    private int cv7;
    private int cv8;
    private int cv9;
    private int cv10;
    private int cv11;
    private int cv12;
    private int cv13;
    private int cv14;
    private int cv15;
    private int cv16;
    private int cv17;
    private int cv18;
    private int cv19;
    private int cv20;

    private int lp1;
    private int lp2;
    private int lp3;
    private int lp4;
    private int lp5;
    private int lp6;
    private int lp7;
    private int lp8;
    private int lp9;
    private int lp10;
    private int lp11;
    private int lp12;
    private int lp13;
    private int lp14;
    private int lp15;
    private int lp16;
    private int lp17;
    private int lp18;
    private int lp19;
    private int lp20;


    public LabellingWorkHistoryEntity toEntity() {
        return LabellingWorkHistoryEntity.builder()
                .historyId(historyId)
                .userId(userId)
                .dataType(dataType)

                .uv1(uv1)
                .uv2(uv2)
                .uv3(uv3)
                .uv4(uv4)
                .uv5(uv5)
                .uv6(uv6)
                .uv7(uv7)
                .uv8(uv8)
                .uv9(uv9)
                .uv10(uv10)

                .cv1(cv1)
                .cv2(cv2)
                .cv3(cv3)
                .cv4(cv4)
                .cv5(cv5)
                .cv6(cv6)
                .cv7(cv7)
                .cv8(cv8)
                .cv9(cv9)
                .cv10(cv10)
                .cv11(cv11)
                .cv12(cv12)
                .cv13(cv13)
                .cv14(cv14)
                .cv15(cv15)
                .cv16(cv16)
                .cv17(cv17)
                .cv18(cv18)
                .cv19(cv19)
                .cv20(cv20)

                .lp1(lp1)
                .lp2(lp2)
                .lp3(lp3)
                .lp4(lp4)
                .lp5(lp5)
                .lp6(lp6)
                .lp7(lp7)
                .lp8(lp8)
                .lp9(lp9)
                .lp10(lp10)
                .lp11(lp11)
                .lp12(lp12)
                .lp13(lp13)
                .lp14(lp14)
                .lp15(lp15)
                .lp16(lp16)
                .lp17(lp17)
                .lp18(lp18)
                .lp19(lp19)
                .lp20(lp20)

                .build();
    }


    @Builder
    public LabellingWorkHistoryDto(int historyId, String userId, String dataType,
                                      int uv1, int uv2, int uv3, int uv4, int uv5, int uv6, int uv7, int uv8, int uv9, int uv10,
                                      int cv1, int cv2, int cv3, int cv4, int cv5, int cv6, int cv7, int cv8, int cv9, int cv10,
                                      int cv11, int cv12, int cv13, int cv14, int cv15, int cv16, int cv17, int cv18, int cv19, int cv20,
                                      int lp1, int lp2, int lp3, int lp4, int lp5, int lp6, int lp7, int lp8, int lp9, int lp10,
                                      int lp11, int lp12, int lp13, int lp14, int lp15, int lp16, int lp17, int lp18, int lp19, int lp20) {

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
