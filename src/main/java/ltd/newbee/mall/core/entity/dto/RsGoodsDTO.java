package ltd.newbee.mall.core.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class RsGoodsDTO implements Serializable {

    private static final long serialVersionUID = 6647665932731822353L;

    private Long goodsId;

    private String goodsName;

    private String goodsIntro;

    private String goodsCoverImg;

    private Long goodsCategoryId;

    private Integer originalPrice;

    private Integer sellingPrice;

    private Integer stockNum;

    private String tag;

    private Byte goodsSellStatus;
}
