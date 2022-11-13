package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 搜索列表页商品VO
 */
@Data
public class SearchPageGoodsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4186374032252933793L;
    private Long goodsId;

    private String goodsName;

    private String goodsIntro;

    private String goodsCoverImg;

    private Integer sellingPrice;

}
