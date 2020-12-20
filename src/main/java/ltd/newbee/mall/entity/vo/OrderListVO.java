package ltd.newbee.mall.entity.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderListVO {
    private Long orderId;

    private String orderNo;

    private Integer totalPrice;

    private Byte payType;

    private Byte orderStatus;

    private String orderStatusString;

    private String userAddress;

    private Date createTime;

    private List<OrderItemVO> newBeeMallOrderItemVOS;
}
