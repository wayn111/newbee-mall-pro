package ltd.newbee.mall.event;

import lombok.Getter;
import lombok.Setter;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.entity.vo.ShopCatVO;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.util.List;

@Getter
@Setter
public class OrderEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = -5946887164851822782L;

    private String orderNo;

    private MallUserVO mallUserVO;

    private Long couponUserId;

    private List<ShopCatVO> shopcatVOList;


    public OrderEvent(String orderNo, MallUserVO mallUserVO, Long couponUserId, List<ShopCatVO> shopcatVOList) {
        super(orderNo);
        this.orderNo = orderNo;
        this.mallUserVO = mallUserVO;
        this.couponUserId = couponUserId;
        this.shopcatVOList = shopcatVOList;
    }
}
