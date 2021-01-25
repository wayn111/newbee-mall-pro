package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

/**
 * 商城首页展示VO对象
 */
@Data
public class IndexConfigGoodsVO {
    private Long goodsId;

    private String goodsName;

    private String goodsIntro;

    private String goodsCoverImg;

    private Integer sellingPrice;

    private String tag;
}
