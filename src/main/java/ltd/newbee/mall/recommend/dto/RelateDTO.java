package ltd.newbee.mall.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelateDTO {
    // 用户id
    private Integer userId;
    // 业务id
    private Integer productId;
    // 指数
    private Integer index;

}
