package ltd.newbee.mall.task;

import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.manager.OrderUnPaidManager;
import ltd.newbee.mall.util.spring.SpringContextUtil;

/**
 * 未支付订单超时自动取消任务
 */
@Slf4j
public class OrderUnPaidTask extends Task {

    /**
     * 默认延迟时间30秒，单位毫秒
     */
    private static final long DELAY_TIME = 30 * 1000;

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
        log.info("系统开始处理延时任务---订单超时未付款--- {}", this.orderId);
        OrderUnPaidManager orderUnPaidManager = SpringContextUtil.getBean(OrderUnPaidManager.class);
        try {
            orderUnPaidManager.doUnPaidTask(this.orderId);
        } catch (Exception e) {
            log.error("---------------订单orderId:{},未支付超时取消成功", orderId, e);
        }
        log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
    }
}
