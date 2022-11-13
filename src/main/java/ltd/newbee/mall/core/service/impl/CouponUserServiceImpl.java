package ltd.newbee.mall.core.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import ltd.newbee.mall.core.dao.CouponDao;
import ltd.newbee.mall.core.dao.CouponUserDao;
import ltd.newbee.mall.core.entity.Coupon;
import ltd.newbee.mall.core.entity.CouponUser;
import ltd.newbee.mall.core.service.CouponService;
import ltd.newbee.mall.core.service.CouponUserService;
import ltd.newbee.mall.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
@AllArgsConstructor
public class CouponUserServiceImpl extends ServiceImpl<CouponUserDao, CouponUser> implements CouponUserService {

    private CouponService couponService;

    private CouponDao couponDao;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveCouponUser(Long couponId, Long userId) {
        Coupon coupon = couponService.getById(couponId);
        if (coupon.getCouponLimit() != 0) {
            long count = count(new QueryWrapper<CouponUser>()
                    .eq("user_id", userId)
                    .eq("coupon_id", coupon.getCouponId()));
            if (count != 0) {
                throw new BusinessException("优惠卷已经领过了,无法再次领取！");
            }
        }
        if (coupon.getCouponTotal() != 0) {
            long count = count(new QueryWrapper<CouponUser>()
                    .eq("coupon_id", coupon.getCouponId()));
            if (count >= coupon.getCouponTotal()) {
                throw new BusinessException("优惠卷已经领完了！");
            }
            if (couponDao.reduceCouponTotal(couponId) <= 0) {
                throw new BusinessException("优惠卷领取失败！");
            }
        }
        CouponUser couponUser = new CouponUser();
        couponUser.setUserId(userId);
        couponUser.setCouponId(couponId);
        Date endDate = calculateEndDate(coupon.getDays());
        couponUser.setStartTime(new Date());
        couponUser.setEndTime(endDate);
        couponUser.setCreateTime(new Date());
        return save(couponUser);
    }

    @Override
    public Coupon getCoupon(Long orderId) {
        CouponUser couponUser = getOne(new QueryWrapper<CouponUser>().eq("order_id", orderId));
        if (couponUser == null) {
            return null;
        }
        return couponService.getById(couponUser.getCouponId());
    }

    @Override
    public Date calculateEndDate(Short days) {
        LocalDate startLocalDate = LocalDate.now();
        LocalDate endLocalDate = startLocalDate.plusDays(days);
        ZoneId zone = ZoneId.systemDefault();
        return Date.from(endLocalDate.atStartOfDay().atZone(zone).toInstant());
    }
}
