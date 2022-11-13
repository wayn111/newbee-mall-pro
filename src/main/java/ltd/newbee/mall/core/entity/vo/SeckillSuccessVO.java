package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户秒杀成功VO
 */
@Data
public class SeckillSuccessVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8083900795250896233L;
    private Long seckillSuccessId;

    private String md5;

}
