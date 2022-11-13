package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class MyCouponVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3630793779817995152L;

    private Long couponUserId;

    private Long userId;

    private Long couponId;

    private String name;

    private String couponDesc;

    private Integer discount;

    private Integer min;

    private Byte goodsType;

    private String goodsValue;

    private Date startTime;

    private Date endTime;

}
