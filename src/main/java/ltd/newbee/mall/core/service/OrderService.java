package ltd.newbee.mall.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.vo.*;

import java.util.List;

public interface OrderService extends IService<Order> {

    /**
     * 查询用户订单列表
     *
     * @param page  分页对象
     * @param order 订单对象
     * @return 分页数据
     */
    IPage<OrderListVO> selectMyOrderPage(Page<OrderListVO> page, Order order);

    /**
     * 后台分页查询订单列表
     *
     * @param page    分页对象
     * @param orderVO 订单VO对象
     * @return 分页数据
     */
    IPage<Order> selectPage(Page<Order> page, OrderVO orderVO);

    /**
     * 生成订单
     *
     * @param mallUserVO    用户VO对象
     * @param couponUserId  用户使用优惠劵表ID
     * @param shopcatVOList 购物车VO集合
     * @return 订单号
     */
    String saveOrder(MallUserVO mallUserVO, Long couponUserId, List<ShopCatVO> shopcatVOList);

    /**
     * 生成秒杀订单
     *
     * @param seckillSuccessId 秒杀成功ID
     * @param userVO           用户对象
     * @return 订单号
     */
    String seckillSaveOrder(Long seckillSuccessId, MallUserVO userVO);

    /**
     * 计算商城订单交易金额
     *
     * @param dayNum 查询天数
     * @return 返回参数天内交易金额集合
     */
    List<DayTransactionAmountVO> countMallTransactionAmount(Integer dayNum);
}
