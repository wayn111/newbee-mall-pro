package ltd.newbee.mall.controller.vo;

import lombok.Data;

@Data
public class ShopCatVO {
    private Long cartItemId;

    private Long goodsId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;
}
