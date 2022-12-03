package ltd.newbee.mall.listener;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.OrderItem;
import ltd.newbee.mall.core.entity.Seckill;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.core.service.OrderItemService;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.core.service.SeckillService;
import ltd.newbee.mall.event.SeckillOrderEvent;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.task.OrderUnPaidTask;
import ltd.newbee.mall.task.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SeckillOrderListener implements ApplicationListener<SeckillOrderEvent> {

    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private SeckillService seckillService;
    @Resource
    private TaskService taskService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private PlatformTransactionManager platformTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    @Override
    public void onApplicationEvent(SeckillOrderEvent event) {
        log.info("SeckillOrderListener onApplicationEvent:{}", event);
        String orderNo = event.getOrderNo();
        if (StringUtils.isBlank(orderNo)) {
            return;
        }
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        try {
            Seckill seckill = event.getSeckill();
            MallUserVO userVO = event.getUserVO();
            Long nowTime = event.getNowTime();
            Long seckillId = seckill.getSeckillId();
            long endTime = seckill.getSeckillEnd().getTime();
            // 减库存
            if (!seckillService.reduceStock(seckillId, new Date(nowTime))) {
                throw new BusinessException("秒杀商品减库存失败");
            }
            Long goodsId = seckill.getGoodsId();
            Goods goods = goodsService.getById(goodsId);
            // 保存订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setTotalPrice(seckill.getSeckillPrice());
            order.setUserId(userVO.getUserId());
            order.setUserAddress(userVO.getAddress());
            String extraInfo = "";
            order.setExtraInfo(extraInfo);
            if (!orderService.save(order)) {
                throw new BusinessException("生成订单内部异常");
            }
            // 记录购买过的用户
            redisCache.setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userVO.getUserId());
            long endExpireTime = endTime / 1000;
            long nowExpireTime = nowTime / 1000;
            redisCache.expire(Constants.SECKILL_SUCCESS_USER_ID + seckillId, endExpireTime - nowExpireTime, TimeUnit.SECONDS);

            // 保存订单商品项
            OrderItem orderItem = new OrderItem();
            Long orderId = order.getOrderId();
            orderItem.setOrderId(orderId);
            orderItem.setSeckillId(seckillId);
            orderItem.setGoodsId(goods.getGoodsId());
            orderItem.setGoodsCoverImg(goods.getGoodsCoverImg());
            orderItem.setGoodsName(goods.getGoodsName());
            orderItem.setGoodsCount(1);
            orderItem.setSellingPrice(seckill.getSeckillPrice());
            if (!orderItemService.save(orderItem)) {
                throw new BusinessException("生成订单内部异常");
            }
            // 秒杀订单1分钟未支付超期任务
            taskService.addTask(new OrderUnPaidTask(orderId, 60 * 1000));
            platformTransactionManager.commit(transaction);
            redisCache.setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + orderNo, Constants.SUCCESS, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            platformTransactionManager.rollback(transaction);
            redisCache.setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + orderNo, "保存订单内部异常", 10, TimeUnit.MINUTES);
        }

    }
}
