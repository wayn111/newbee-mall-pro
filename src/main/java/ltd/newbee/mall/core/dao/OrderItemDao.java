package ltd.newbee.mall.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ltd.newbee.mall.core.entity.OrderItem;
import ltd.newbee.mall.core.entity.vo.OrderItemVO;

import java.util.List;


public interface OrderItemDao extends BaseMapper<OrderItem> {
    List<OrderItemVO> selectByOrderId(Long orderId);

    List<OrderItemVO> selectByOrderIds(List<Long> orderIds);
}
