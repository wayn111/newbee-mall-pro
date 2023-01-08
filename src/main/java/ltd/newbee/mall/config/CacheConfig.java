package ltd.newbee.mall.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
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
        return clientConfigurationBuilder -> {
            LettuceClientConfiguration clientConfiguration = clientConfigurationBuilder.build();
            ClientOptions clientOptions = clientConfiguration.getClientOptions().orElseGet(ClientOptions::create);
            ClientOptions build = clientOptions.mutate().build();
            SocketOptions.KeepAliveOptions.Builder builder = build.getSocketOptions().getKeepAlive().mutate();
            builder.enable(true);
            builder.idle(Duration.ofSeconds(30));
            SocketOptions.Builder socketOptionsBuilder = clientOptions.getSocketOptions().mutate();
            SocketOptions.KeepAliveOptions keepAliveOptions = builder.build();
            socketOptionsBuilder.keepAlive(keepAliveOptions);
            SocketOptions socketOptions = socketOptionsBuilder.build();
            ClientOptions clientOptions1 = ClientOptions.builder().socketOptions(socketOptions).build();
            clientConfigurationBuilder.clientOptions(clientOptions1);
        };
    }

    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    private RedisSerializer<Object> valueSerializer() {
        return new GenericFastJsonRedisSerializer();
    }

}
