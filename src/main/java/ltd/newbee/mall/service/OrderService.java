package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.controller.vo.OrderListVO;
import ltd.newbee.mall.controller.vo.ShopCatVO;
import ltd.newbee.mall.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    IPage selectMyOrderPage(Page<OrderListVO> page, Order order);

    IPage selectPage(Page<Order> page, Order order);

    String saveOrder(MallUserVO mallUserVO, List<ShopCatVO> shopcatVOList);

}
