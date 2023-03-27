package ltd.newbee.mall.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.NettyCustomizer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollChannelOption;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setValueSerializer(valueSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * lettuce客户端配置
     *
     * @return LettuceClientConfigurationBuilderCustomizer
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return builder -> {
            builder.clientOptions(ClientOptions.builder().socketOptions(SocketOptions.builder()
                    .keepAlive(SocketOptions.KeepAliveOptions.builder()
                            .enable(true)
                            .idle(Duration.ofMinutes(3))
                            .count(3)
                            .interval(Duration.ofSeconds(10))
                            .build()
                    ).build()).build());
        };
    }

    @Bean
    public ClientResources clientResources() {
        NettyCustomizer nettyCustomizer = new NettyCustomizer() {
            @Override
            public void afterChannelInitialized(Channel channel) {
                // channel.pipeline().addLast(new IdleStateHandler(40, 0, 0));
                // channel.pipeline().addLast(new ChannelDuplexHandler() {
                //     @Override
                //     public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                //         if (evt instanceof IdleStateEvent) {
                //             ctx.disconnect();
                //         }
                //     }
                // });
            }

            @Override
            public void afterBootstrapInitialized(Bootstrap bootstrap) {
                bootstrap.option(EpollChannelOption.TCP_USER_TIMEOUT, 100);
            }
        };
        return ClientResources.builder().nettyCustomizer(nettyCustomizer).build();
    }

    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    private RedisSerializer<Object> valueSerializer() {
        return new GenericFastJsonRedisSerializer();
    }

}
