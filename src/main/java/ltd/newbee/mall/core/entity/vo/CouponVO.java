package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CouponVO implements Serializable {

    private static final long serialVersionUID = 7392480821618562611L;

    private Long couponId;

    private String name;

    private String couponDesc;

    private Integer couponTotal;

    private boolean saleOut;

    private Integer discount;

    private Integer min;

    private Byte couponLimit;

    private Byte couponType;

    private Byte status;

    private String goodsType;

    private String goodsValue;

    private String code;

    private Short days;

    private boolean hasReceived;

    private String startTime;

    private String endTime;
}
