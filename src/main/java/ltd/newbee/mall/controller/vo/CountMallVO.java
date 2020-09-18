package ltd.newbee.mall.controller.vo;

import lombok.Data;

/**
 * 日交易金额统计VO对象
 */
@Data
public class CountMallVO {

    private String days;

    private Long totalPrice;
}
