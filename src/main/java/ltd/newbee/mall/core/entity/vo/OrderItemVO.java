package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OrderItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2726912066641164745L;
    private Long orderId;

    private Long goodsId;

    private Long categoryId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;

}
