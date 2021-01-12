package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

@Data
public class OrderItemVO {
    private Long goodsId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;

}
