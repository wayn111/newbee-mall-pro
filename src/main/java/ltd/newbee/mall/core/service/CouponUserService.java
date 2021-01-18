package ltd.newbee.mall.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.core.entity.Coupon;
import ltd.newbee.mall.core.entity.CouponUser;

public interface CouponUserService extends IService<CouponUser> {


    /**
     * 用户领取优惠劵
     *
     * @param couponId 优惠劵ID
     * @param userId   用户ID
     * @return boolean
     */
    boolean saveCouponUser(Long couponId, Long userId);

    /**
     * 获取优惠劵信息
     *
     * @param orderId 订单ID
     * @return 优惠劵信息
     */
    Coupon getCoupon(Long orderId);
}
