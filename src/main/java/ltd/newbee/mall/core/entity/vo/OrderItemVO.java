package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderItemVO implements Serializable {
    private static final long serialVersionUID = 2726912066641164745L;
    private Long goodsId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;

}
