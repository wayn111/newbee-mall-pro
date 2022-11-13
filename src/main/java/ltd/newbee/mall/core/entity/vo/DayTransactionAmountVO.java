package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 日交易金额VO对象
 */
@Data
public class DayTransactionAmountVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -961725122511119233L;
    private String days;

    private Long totalPrice;
}
