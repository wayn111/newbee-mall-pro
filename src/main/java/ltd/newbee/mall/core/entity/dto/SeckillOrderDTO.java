package ltd.newbee.mall.core.entity.dto;

import lombok.Getter;
import lombok.Setter;
import ltd.newbee.mall.core.entity.Seckill;
import ltd.newbee.mall.core.entity.vo.MallUserVO;

import java.io.Serial;

@Getter
@Setter
public class SeckillOrderDTO {

    private static final long serialVersionUID = -5946887164851822782L;

    private String orderNo;

    private Seckill seckill;

    private MallUserVO userVO;

    private Long nowTime;

    public SeckillOrderDTO(String orderNo, Seckill seckill, MallUserVO userVO, Long nowTime) {
        this.orderNo = orderNo;
        this.seckill = seckill;
        this.userVO = userVO;
        this.nowTime = nowTime;
    }
}
