package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.controller.vo.*;
import ltd.newbee.mall.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {

    /**
     * 查询用户订单列表
     * @param page 分页对象
     * @param order 订单对象
     * @return 分页数据
     */
    IPage selectMyOrderPage(Page<OrderListVO> page, Order order);

    /**
     * 后台分页查询订单列表
     * @param page 分页对象
     * @param orderVO 订单VO对象
     * @return 分页数据
     */
    IPage selectPage(Page<Order> page, OrderVO orderVO);

    /**
     * 生成订单
     * @param mallUserVO 用户VO对象
     * @param couponUserId 用户使用优惠劵表ID
     * @param shopcatVOList 购物车VO集合
     * @return 订单号
     */
    String saveOrder(MallUserVO mallUserVO, Long couponUserId, List<ShopCatVO> shopcatVOList);

    /**
     * 计算商城订单交易金额
     * @return 日交易金额统计VO集合
     */
    List<CountMallVO> countMallTransactionAmount();
}
