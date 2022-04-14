package ltd.newbee.mall.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.entity.CouponUser;
import ltd.newbee.mall.core.service.CouponUserService;
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
public class CouponUserExpireTask {

    @Autowired
    private CouponUserService couponUserService;

    /**
     * 每天凌晨2点执行任务，检查用户领取的优惠卷是否过期
     * cron = "0 0 2 * * ?"
     */
    @Async
    @Scheduled(fixedDelay = 1000 * 30)
    public void checkCouponStatus() {
        log.info("检查用户领取的优惠卷是否过期任务:开始");
        try {
            List<CouponUser> list = couponUserService.list(Wrappers.<CouponUser>lambdaQuery().eq(CouponUser::getStatus, 0));
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
            if (CollectionUtils.isNotEmpty(couponUserIdList)) {
                couponUserService.lambdaUpdate()
                        .set(CouponUser::getStatus, 2)
                        .in(CouponUser::getCouponUserId, couponUserIdList)
                        .update();
                log.info("更新couponUserIdList:{}", couponUserIdList);
            }
        } catch (Exception e) {
            log.error("检查用户领取的优惠卷执行失败：{}", e.getMessage(), e);
        }
        log.info("检查用户领取的优惠卷是否过期任务:结束");
    }
}
