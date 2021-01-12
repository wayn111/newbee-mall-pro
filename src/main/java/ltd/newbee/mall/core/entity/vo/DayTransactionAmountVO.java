package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

/**
 * 日交易金额VO对象
 */
@Data
public class DayTransactionAmountVO {

    private String days;

    private Long totalPrice;
}
