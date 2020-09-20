package ltd.newbee.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.controller.vo.DayTransactionAmountVO;
import ltd.newbee.mall.controller.vo.OrderListVO;
import ltd.newbee.mall.controller.vo.OrderVO;
import ltd.newbee.mall.entity.Order;

import java.util.List;

public interface OrderDao extends BaseMapper<Order> {

    IPage selectListVOPage(Page<OrderListVO> page, Order order);

    IPage selectListPage(Page<Order> page, OrderVO order);

    List<DayTransactionAmountVO> countMallTransactionAmount(Integer dayNum);
}
