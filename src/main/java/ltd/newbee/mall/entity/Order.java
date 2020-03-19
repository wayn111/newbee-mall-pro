package ltd.newbee.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tb_newbee_mall_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String orderNo;

    private Long userId;

    private Double totalPrice;

    private Byte payStatus;

    private Byte payType;

    private Date payTime;

    private Byte orderStatus;

    private String extraInfo;

    private String userAddress;

    @TableLogic
    private Byte isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
