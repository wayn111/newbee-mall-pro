package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.entity.CouponUser;

public interface CouponUserService extends IService<CouponUser> {


    boolean saveCouponUser(Long couponId, Long userId);

    Coupon getCoupon(Long orderId);
}
