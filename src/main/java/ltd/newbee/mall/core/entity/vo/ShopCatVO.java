package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 购物车VO对象
 */
@Data
public class ShopCatVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2806471426638763736L;
    private Long cartItemId;

    private Long goodsId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;
}
