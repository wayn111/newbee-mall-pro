package ltd.newbee.mall.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelateDTO {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 商品id
     */
    private Long productId;
    /**
     * 类目id
     */
    private Long categoryId;
    /**
     * 指数
     */
    private Integer index;

}
