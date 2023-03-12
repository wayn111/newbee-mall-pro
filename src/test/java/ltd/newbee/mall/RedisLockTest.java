package ltd.newbee.mall;


import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.redis.RedisLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisLockTest {


    @Autowired
    private RedisLock redisLock;

    /**
     * 自动续期测试
     */
    @Test
    public void redisLockNeNewTest() {
        String key = "test";
        try {
            log.info("---申请加锁");
            if (redisLock.lock(key, 10)) {
                // 模拟任务执行15秒
                log.info("---加锁成功");
                Thread.sleep(15000);
                log.info("---执行完毕");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            redisLock.unLock(key);
        }
    }


    /**
     * 多个线程释放自身锁测试
     * @throws IOException
     */
    @Test
    public void redisLockReleaseSelfTest() throws IOException {
        new Thread(() -> {
            String key = "test";
            try {
                log.info("---申请加锁");
                if (redisLock.lock(key, 10)) {
                    // 模拟任务执行15秒
                    log.info("---加锁成功");
                    Thread.sleep(15000);
                    log.info("---执行完毕");
                } else {
                    log.info("---加锁失败");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                redisLock.unLock(key);
            }
        }, "thread-A").start();
        new Thread(() -> {
            String key = "test";
            try {
                Thread.sleep(100L);
                log.info("---申请加锁");
                if (redisLock.lock(key, 10)) {
                    // 模拟任务执行15秒
                    log.info("---加锁成功");
                    Thread.sleep(15000);
                    log.info("---执行完毕");
                } else {
                    log.info("---加锁失败");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                redisLock.unLock(key);
            }
        }, "thread-B").start();
        System.in.read();
    }


    /**
     * 锁重入性测试
     */
    @Test
    public void redisLockReEntryTest() {
        String key = "test";
        try {
            log.info("---申请加锁");
            if (redisLock.lock(key, 10)) {
                // 模拟任务执行15秒
                log.info("---加锁第一次成功");
                if (redisLock.lock(key, 10)) {
                    // 模拟任务执行15秒
                    log.info("---加锁第二次成功");
                    Thread.sleep(15000);
                    log.info("---加锁第二次执行完毕");
                } else {
                    log.info("---加锁第二次失败");
                }
                Thread.sleep(15000);
                log.info("---加锁第一次执行完毕");
            } else {
                log.info("---加锁第一次失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            redisLock.unLock(key);
        }
    }
}
