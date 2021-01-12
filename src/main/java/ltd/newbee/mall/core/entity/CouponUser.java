package ltd.newbee.mall.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tb_newbee_mall_coupon_user")
public class CouponUser implements Serializable {
    private static final long serialVersionUID = 3630793779817995152L;

    @TableId(type = IdType.AUTO)
    private Long couponUserId;

    private Long userId;

    private Long couponId;

    private Byte status;

    private Date usedTime;

    private Date startTime;

    private Date endTime;

    private Long orderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private Byte isDeleted;
}
