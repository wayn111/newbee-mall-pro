package ltd.newbee.mall.listener;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.CouponUser;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.OrderItem;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.entity.vo.ShopCatVO;
import ltd.newbee.mall.core.service.*;
import ltd.newbee.mall.event.OrderEvent;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.task.OrderUnPaidTask;
import ltd.newbee.mall.task.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderListener implements ApplicationListener<OrderEvent> {

    @Resource
    private ShopCatService shopCatService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private CouponUserService couponUserService;
    @Resource
    private TaskService taskService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private PlatformTransactionManager platformTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    @Override
    public void onApplicationEvent(OrderEvent event) {
        log.info("OrderListener onApplicationEvent:{}", event);
        String orderNo = event.getOrderNo();
        if (StringUtils.isBlank(orderNo)) {
            return;
        }
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        try {
            Long couponUserId = event.getCouponUserId();
            MallUserVO mallUserVO = event.getMallUserVO();
            List<ShopCatVO> shopcatVOList = event.getShopcatVOList();
            List<Long> cartItemIdList = shopcatVOList.stream().map(ShopCatVO::getCartItemId).collect(Collectors.toList());

            // 删除购物项
            if (!shopCatService.removeByIds(cartItemIdList)) {
                throw new BusinessException("删除购物车异常");
            }
            shopcatVOList.forEach(shopCatVO -> {
                // 更新商品库存
                if (!goodsService.reduceStock(shopCatVO.getGoodsId(), shopCatVO.getGoodsCount())) {
                    throw new BusinessException("扣减商品库存失败");
                }
            });
            int priceTotal = 0;
            for (ShopCatVO shopCatVO : shopcatVOList) {
                priceTotal += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
            }
            // 保存订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setTotalPrice(priceTotal);
            order.setUserId(mallUserVO.getUserId());
            order.setUserAddress(mallUserVO.getAddress());
            // 订单body字段，用来作为生成支付单描述信息，暂时未接入第三方支付接口，故该字段暂时设为空字符串
            String extraInfo = "";
            order.setExtraInfo(extraInfo);
            // 生成订单项并保存订单项纪录
            if (!orderService.save(order)) {
                throw new BusinessException("保存订单异常");
            }
            Long orderId = order.getOrderId();
            // 生成所有的订单项快照，并保存至数据库
            List<OrderItem> orderItems = shopcatVOList.stream().map(shopCatVO -> {
                OrderItem orderItem = new OrderItem();
                BeanUtils.copyProperties(shopCatVO, orderItem);
                orderItem.setOrderId(orderId);
                return orderItem;
            }).collect(Collectors.toList());

            if (!orderItemService.saveBatch(orderItems)) {
                throw new BusinessException("保存订单内部异常");
            }
            // 如果使用了优惠卷，则更新优惠卷状态
            if (couponUserId != null) {
                CouponUser couponUser = new CouponUser();
                couponUser.setCouponUserId(couponUserId);
                couponUser.setStatus((byte) 1);
                couponUser.setUsedTime(new Date());
                couponUser.setOrderId(order.getOrderId());
                couponUserService.updateById(couponUser);
            }
            // 订单支付超期任务
            taskService.addTask(new OrderUnPaidTask(orderId));
            platformTransactionManager.commit(transaction);
            redisCache.setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + orderNo, Constants.SUCCESS, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            platformTransactionManager.rollback(transaction);
            redisCache.setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + orderNo, "保存订单内部异常", 10, TimeUnit.MINUTES);
        }

    }
}
