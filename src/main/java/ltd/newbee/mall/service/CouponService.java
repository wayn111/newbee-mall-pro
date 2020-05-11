package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.controller.vo.CouponVO;
import ltd.newbee.mall.controller.vo.MyCouponVO;
import ltd.newbee.mall.entity.Coupon;

import java.util.List;

public interface CouponService extends IService<Coupon> {

    IPage selectPage(Page<Coupon> page, Coupon coupon);

    /**
     * 查询可用的优惠卷
     *
     * @return
     */
    List<CouponVO> selectAvailableCoupon();

    List<MyCouponVO> selectMyCoupons(Long userId);
}
