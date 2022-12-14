package ltd.newbee.mall.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.dao.GoodsDao;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.OrderItem;
import ltd.newbee.mall.core.service.CouponService;
import ltd.newbee.mall.core.service.OrderItemService;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.core.service.SeckillService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.util.spring.SpringContextUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class OrderUnPaidManager {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private CouponService couponService;

    @Transactional(rollbackFor = Exception.class)
    public void doUnPaidTask(Long orderId) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(String.format("订单不存在，orderId:%s", orderId));
        }
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            throw new BusinessException(String.format("订单状态错误，order:%s", order));
        }

        // 设置订单为已取消状态
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus());
        order.setUpdateTime(new Date());
        if (!orderService.updateById(order)) {
            throw new BusinessException("更新数据已失效");

        }

        // 商品货品数量增加
        LambdaQueryWrapper<OrderItem> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemService.list(queryWrapper);
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getSeckillId() != null) { // 秒杀单商品项处理
                Long seckillId = orderItem.getSeckillId();
                SeckillService seckillService = SpringContextUtil.getBean(SeckillService.class);
                RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);
                if (!seckillService.addStock(seckillId)) {
                    throw new BusinessException("秒杀商品货品库存增加失败");

                }
                redisCache.increment(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
                redisCache.deleteCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, order.getUserId());
            } else { // 普通单商品项处理
                Long goodsId = orderItem.getGoodsId();
                Integer goodsCount = orderItem.getGoodsCount();
                if (!goodsDao.addStock(goodsId, goodsCount)) {
                    throw new BusinessException("秒杀商品货品库存增加失败");
                }
            }
        }

        // 返还优惠券
        couponService.releaseCoupon(orderId);
        log.info("---------------订单orderId:{},未支付超时取消成功", orderId);
    }
}
