package ltd.newbee.mall.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.entity.CouponUser;
import ltd.newbee.mall.core.service.CouponUserService;
import ltd.newbee.mall.redis.RedisCache;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
public class RedisTask {

    @Autowired
    private RedisCache redisCache;

    @Async
    @Scheduled(fixedDelay = 1000 * 30)
    public void checkCouponStatus() {
        redisCache.getCacheObject("test");
    }
}
