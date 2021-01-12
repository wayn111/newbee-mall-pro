package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderDetailVO {

    private String orderNo;

    private Integer totalPrice;

    private Byte payStatus;

    private String payStatusString;

    private Byte payType;

    private String payTypeString;

    private Date payTime;

    private Byte orderStatus;

    private String orderStatusString;

    private String userAddress;

    private Date createTime;

    private List<OrderItemVO> newBeeMallOrderItemVOS;
}
