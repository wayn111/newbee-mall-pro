package ltd.newbee.mall.entity.vo;

import lombok.Data;

/**
 * 购物车VO对象
 */
@Data
public class ShopCatVO {
    private Long cartItemId;

    private Long goodsId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;
}
