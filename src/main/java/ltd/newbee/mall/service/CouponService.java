package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.Coupon;

public interface CouponService extends IService<Coupon> {

    IPage selectPage(Page<Coupon> page, Coupon coupon);
}
