package ltd.newbee.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.OrderItem;
import ltd.newbee.mall.core.service.CouponService;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.core.service.OrderItemService;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.util.spring.SpringContextUtil;

import java.util.Date;
import java.util.List;

/**
 * 未支付订单超时自动取消任务
 */
@Slf4j
public class OrderUnPaidTask extends Task {

    /**
     * 默认延迟时间30分钟，单位毫秒
     */
    private static final long DELAY_TIME = 30 * 60 * 1000;

    /**
     * 订单id
     */
    private final Long orderId;

    public OrderUnPaidTask(Long orderId, long delayInMilliseconds) {
        super("OrderUnPaidTask-" + orderId, delayInMilliseconds);
        this.orderId = orderId;
    }

    public OrderUnPaidTask(Long orderId) {
        super("OrderUnPaidTask-" + orderId, DELAY_TIME);
        this.orderId = orderId;
    }

    @Override
    public void run() {
        log.info("系统开始处理延时任务---订单超时未付款---" + this.orderId);

        OrderService orderService = SpringContextUtil.getBean(OrderService.class);
        OrderItemService orderItemService = SpringContextUtil.getBean(OrderItemService.class);
        GoodsService goodsService = SpringContextUtil.getBean(GoodsService.class);
        CouponService couponService = SpringContextUtil.getBean(CouponService.class);

        Order order = orderService.getById(orderId);
        if (order == null) {
            return;
        }
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            return;
        }

        // 设置订单为已取消状态
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus());
        order.setUpdateTime(new Date());
        if (!orderService.updateById(order)) {
            throw new RuntimeException("更新数据已失效");
        }

        // 商品货品数量增加
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem orderItem : orderItems) {
            Long goodsId = orderItem.getGoodsId();
            Integer goodsCount = orderItem.getGoodsCount();
            if (!goodsService.addStock(goodsId, goodsCount)) {
                throw new RuntimeException("商品货品库存增加失败");
            }
        }

        // 返还优惠券
        couponService.releaseCoupon(orderId);
        log.info("系统结束处理延时任务---订单超时未付款---" + this.orderId);
    }
}
