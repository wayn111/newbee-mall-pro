package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商城首页展示VO对象
 */
@Data
public class IndexConfigGoodsVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8532534635955415765L;
    private Long goodsId;

    private String goodsName;

    private String goodsIntro;

    private String goodsCoverImg;

    private Integer sellingPrice;

    private String tag;
}
