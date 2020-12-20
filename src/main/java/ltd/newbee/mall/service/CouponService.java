package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.entity.vo.CouponVO;
import ltd.newbee.mall.entity.vo.MyCouponVO;
import ltd.newbee.mall.entity.vo.ShopCatVO;

import java.util.List;

public interface CouponService extends IService<Coupon> {

    /**
     * 分页查询
     * @param page 分页对象
     * @param coupon 优惠劵对象
     * @return 分页数据
     */
    IPage selectPage(Page<Coupon> page, CouponVO coupon);

    /**
     * 查询可用的优惠卷
     *
     * @return 可用优惠劵集合
     * @param userId 用户ID
     */
    List<CouponVO> selectAvailableCoupon(Long userId);

    /**
     * 获取用户的优惠劵
     *
     * @param shopCatVOS 购物车商品数据集合
     * @param priceTotal 总价
     * @param userId     用户ID
     * @return 该订单可用优惠劵集合
     */
    List<MyCouponVO> selectMyCoupons(List<ShopCatVO> shopCatVOS, int priceTotal, Long userId);
}
