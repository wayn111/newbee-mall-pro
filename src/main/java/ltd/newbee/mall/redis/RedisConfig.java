package ltd.newbee.mall.redis;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;

import java.time.Duration;

/**
 * redis配置
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host = "127.0.0.1";

    @Value("${spring.data.redis.port}")
    private int port = 6379;

    //timeout for jedis try to connect to redis server, not expire time! In milliseconds
    @Value("${spring.data.redis.timeout}")
    private int timeout = 0;

    @Value("${spring.data.redis.password}")
    private String password = "";

    @Value("${spring.data.redis.database}")
    private Integer database = 0;

    @Value("${spring.data.redis.jedis.pool.max-idle}")
    private int maxIdle;


    @Value("${spring.data.redis.jedis.pool.max-wait}")
    private int maxWaitMillis;

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));
        jedisPoolConfig.setJmxEnabled(false);
        return jedisPoolConfig;
    }

    @Bean
    public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
        JedisPool jedisPool;
        if (StringUtils.isNotEmpty(password)) {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password, database);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, null, database);
        }
        return jedisPool;
    }

    @Bean
    public UnifiedJedis unifiedJedis(GenericObjectPoolConfig jedisPoolConfig) {
        UnifiedJedis client;
        if (StringUtils.isNotEmpty(password)) {
            client = new JedisPooled(jedisPoolConfig, host, port, timeout, password, database);
        } else {
            client = new JedisPooled(jedisPoolConfig, host, port, timeout, null, database);
        }
        return client;
    }
}
