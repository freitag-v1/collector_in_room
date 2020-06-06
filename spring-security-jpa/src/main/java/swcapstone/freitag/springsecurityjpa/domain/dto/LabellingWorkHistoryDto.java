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
    private int cv1;
    private int cv2;
    private int lp1;
    private int lp2;


    public LabellingWorkHistoryEntity toEntity() {
        return LabellingWorkHistoryEntity.builder()
                .historyId(historyId)
                .userId(userId)
                .dataType(dataType)

                .uv1(uv1)
                .cv1(cv1)
                .cv2(cv2)
                .lp1(lp1)
                .lp2(lp2)
                .build();
    }


    @Builder
    public LabellingWorkHistoryDto(int historyId, String userId, String dataType,
                                   int uv1, int cv1, int cv2, int lp1, int lp2) {

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
