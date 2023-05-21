package ltd.newbee.mall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static ltd.newbee.mall.mq.RabbitMQConstant.*;

/**
 * 直连交换机配置
 */
@Configuration
public class DirectRabbitConfig {


    /************************************ 订单队列、交换机配置begin *******************************************/
    @Bean
    public Queue OrderDirectQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    DirectExchange OrderDirectExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    Binding bindingOrderDirect() {
        return BindingBuilder.bind(OrderDirectQueue()).to(OrderDirectExchange()).with(ROUTE_NAME);
    }
    /************************************ 订单队列、交换机配置end *******************************************/

}
