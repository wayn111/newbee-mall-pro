package ltd.newbee.mall.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.entity.CouponUser;
import ltd.newbee.mall.core.service.CouponUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@EnableAsync
@Configuration
@EnableScheduling
public class TaskConfig {

    @Autowired
    private CouponUserService couponUserService;

    /**
     * 每天凌晨2点执行任务，检查用户领取的优惠卷是否过期<br>
     * cron = "0 0 2 * * ?"
     */
    @Async
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkCouponStatus() {
        log.info("检查用户领取的优惠卷是否过期任务:开始");
        List<CouponUser> list = couponUserService.list(new QueryWrapper<CouponUser>().eq("status", 0));
        if (list == null || list.isEmpty()) {
            log.info("检查用户领取的优惠卷是否过期任务:无过期优惠劵结束");
            return;
        }
        List<Long> couponUserIdList = new ArrayList<>();
        for (CouponUser couponUser : list) {
            if (LocalDate.now().isAfter(couponUser.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
                couponUserIdList.add(couponUser.getCouponUserId());
            }
        }
        couponUserService.update().set("status", 2).in(!couponUserIdList.isEmpty(), "coupon_user_id", couponUserIdList).update();
        log.info("检查用户领取的优惠卷是否过期任务:结束");
    }
}
