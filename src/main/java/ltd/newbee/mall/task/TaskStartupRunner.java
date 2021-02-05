package ltd.newbee.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * 系统启动时添加未支付超时订单任务
 */
@Component
public class TaskStartupRunner implements ApplicationRunner {

    public static final Long UN_PAID_ORDER_EXPIRE_TIME = 30L;
    @Autowired
    private OrderService orderService;
    @Autowired
    private TaskService taskService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Order> orderList = orderService.list(new QueryWrapper<Order>()
                .eq("order_status", OrderStatusEnum.ORDER_PRE_PAY));
        for (Order order : orderList) {
            Date date = order.getCreateTime();
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();

            LocalDateTime add = instant.atZone(zoneId).toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expire = add.plusMinutes(UN_PAID_ORDER_EXPIRE_TIME);
            if (expire.isBefore(now)) {
                // 已经过期，则加入延迟队列立即执行
                taskService.addTask(new OrderUnPaidTask(order.getOrderId(), 0));
            } else {
                // 还没过期，则加入延迟队列
                long delay = ChronoUnit.MILLIS.between(now, expire);
                taskService.addTask(new OrderUnPaidTask(order.getOrderId(), delay));
            }
        }
    }
}
