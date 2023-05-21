package ltd.newbee.mall.mq.config;

import ltd.newbee.mall.mq.reciver.OrderDirectReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作模式rabbitmq配置
 */
@Configuration
public class WorkRabbitConfig {

    /*******************************************订单消费者配置2个*****************************************/
    @Bean
    public OrderDirectReceiver orderWorkReceiver1() {
        return new OrderDirectReceiver(1);
    }
}
