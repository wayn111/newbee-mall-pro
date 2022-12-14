package ltd.newbee.mall.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 *
 * @author ruoyi
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisCache {

    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * string类型递增
     *
     * @param key 缓存的键值
     * @return 递增后返回值
     */
    public Long increment(final String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * string类型递减
     *
     * @param key 缓存的键值
     * @return 递减后返回值
     */
    public Long decrement(final String key) {
        return redisTemplate.opsForValue().decrement(key);

    }

    /**
     * string类型原子递减，不小于-1
     *
     * @param key 缓存的键值
     * @return 递减后返回值
     */
    public Long luaDecrement(final String key) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(buildLuaDecScript(), Long.class);
        Long execute = (Long) redisTemplate.execute(redisScript, Collections.singletonList(key));
        if (execute == null) {
            return -1L;
        }
        return execute;
    }

    /**
     * lua原子自减脚本
     */
    private String buildLuaDecScript() {
        return "local c" +
                "\nc = redis.call('get',KEYS[1])" +
                "\nif c and tonumber(c) < 0 then" +
                "\nreturn c;" +
                "\nend" +
                "\nc = redis.call('decr',KEYS[1])" +
                "\nreturn c;";
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 判断缓存是否存在。
     *
     * @param key 缓存键值
     * @return true=存在；false=不存在
     */
    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection collection) {
        return redisTemplate.delete(collection);
    }

    public long deleteLikesKeyObject(String prefix) {
        return redisTemplate.delete(getLikesKeyList(prefix));
    }

    public <T> List<T> getLikesKeyList(String prefix) {
        // 获取所有的key
        Set<String> keys = redisTemplate.keys(prefix);
        // 批量获取数据
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 往Set中添加指定value
     *
     * @param key   缓存键值
     * @param value 缓存的数据
     * @return 添加元素数量
     */
    public long setCacheSet(final String key, Object... value) {
        Long count = redisTemplate.opsForSet().add(key, value);
        return count == null ? 0 : count;
    }

    /**
     * 删除Set中指定value
     *
     * @param key   缓存键值
     * @param value 缓存的数据
     * @return 删除元素数量
     */
    public long deleteCacheSet(final String key, Object... value) {
        Long count = redisTemplate.opsForSet().remove(key, value);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的set
     *
     * @param key 缓存键值
     * @return 缓存set数据
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断key-set中是否存在value
     *
     * @param key   缓存键值
     * @param value 缓存的数据
     * @return boolean
     */
    public Boolean containsCacheSet(final String key, final Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 缓存zset
     *
     * @param key   缓存键名
     * @param value 缓存键值
     * @param score 分数
     * @return 缓存数据的对象
     */
    public <T> ZSetOperations<String, T> setCacheZset(String key, T value, double score) {
        ZSetOperations operations = redisTemplate.opsForZSet();
        operations.add(key, value, score);
        return operations;
    }

    /**
     * 删除zset
     *
     * @param key   缓存键名
     * @param value 缓存键值
     * @return 删除个数
     */
    public <T> Long deleteZsetObject(String key, T value) {
        ZSetOperations operations = redisTemplate.opsForZSet();
        return operations.remove(key, value);
    }

    /**
     * 获得缓存的set
     *
     * @param key 缓存键名
     * @param min 最低分数
     * @param max 最高分数
     * @return 满足分数区间的键值
     */
    public <T> Set<T> getCacheZset(String key, double min, double max) {
        ZSetOperations operations = redisTemplate.opsForZSet();
        return operations.rangeByScore(key, min, max);
    }

}
