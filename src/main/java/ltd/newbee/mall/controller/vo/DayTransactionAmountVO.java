package ltd.newbee.mall.controller.vo;

import lombok.Data;

/**
 * 日交易金额VO对象
 */
@Data
public class DayTransactionAmountVO {

    private String days;

    private Long totalPrice;
}
