package ltd.newbee.mall.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tb_newbee_mall_coupon")
public class Coupon implements Serializable {

    @Serial
    private static final long serialVersionUID = 7392480821618562611L;

    @TableId(type = IdType.AUTO)
    private Long couponId;

    private String name;

    private String couponDesc;

    private Integer couponTotal;

    private Integer discount;

    private Integer min;

    private Byte couponLimit;

    private Byte couponType;

    private Byte status;

    private Byte goodsType;

    private String goodsValue;

    private String code;

    private Short days;

    private Date createTime;

    private Date updateTime;

    private Byte isDeleted;
}
