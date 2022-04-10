package ltd.newbee.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.util.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 系统启动时添加未支付超时订单任务
 */
@Component
public class SpringBeanStartupRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 设置事件监听异步执行
        SimpleApplicationEventMulticaster multicaster = SpringContextUtil.getBean(SimpleApplicationEventMulticaster.class);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,
                10,
                60L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new CustomizableThreadFactory("newbee—event-task"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        multicaster.setTaskExecutor(threadPoolExecutor);
    }
}
