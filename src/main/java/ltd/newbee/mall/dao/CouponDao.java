package ltd.newbee.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.entity.vo.CouponVO;

import java.util.List;

public interface CouponDao extends BaseMapper<Coupon> {

    IPage selectListPage(Page<Coupon> page, CouponVO coupon);

    List<Coupon> selectAvailableCoupon();
}
