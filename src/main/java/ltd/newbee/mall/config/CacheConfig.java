package ltd.newbee.mall.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.apache.commons.lang3.SystemUtils;
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
        redisTemplate.setHashKeySerializer(keySerializer());
        redisTemplate.setDefaultSerializer(valueSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceCustomizer() {
        return builder -> {
            if (SystemUtils.IS_OS_LINUX) {
                // create your socket options
                SocketOptions socketOptions = SocketOptions.builder()
                        .tcpUserTimeout(SocketOptions.TcpUserTimeoutOptions.builder()
                                .enable(true)
                                .tcpUserTimeout(Duration.ofSeconds(5))
                                .build()
                        )
                        .keepAlive(SocketOptions.KeepAliveOptions.builder()
                                .enable()
                                .idle(Duration.ofSeconds(30))
                                .interval(Duration.ofSeconds(10))
                                .count(3)
                                .build()
                        ).build();
                builder.clientOptions(ClientOptions.builder()
                        .socketOptions(socketOptions)
                        .build())
                ;
            }
        };
    }

    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    private RedisSerializer<Object> valueSerializer() {
        return new GenericFastJsonRedisSerializer();
    }

    /**
     * 指定spring-session的默认序列化方式
     *
     * @return RedisSerializer
     */
    @Bean("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> redisSerializer() {
        return valueSerializer();
    }

}
