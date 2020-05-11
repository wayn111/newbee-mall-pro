package ltd.newbee.mall.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.controller.vo.CouponVO;
import ltd.newbee.mall.controller.vo.MyCouponVO;
import ltd.newbee.mall.dao.CouponDao;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.entity.CouponUser;
import ltd.newbee.mall.service.CouponService;
import ltd.newbee.mall.service.CouponUserService;
import ltd.newbee.mall.util.MyBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponDao, Coupon> implements CouponService {

    @Autowired
    private CouponDao couponDao;

    @Autowired
    private CouponUserService couponUserService;

    @Override
    public IPage selectPage(Page<Coupon> page, Coupon coupon) {
        return couponDao.selectListPage(page, coupon);
    }

    @Override
    public List<CouponVO> selectAvailableCoupon() {
        List<Coupon> coupons = couponDao.selectAvailableCoupon();
        List<CouponVO> couponVOS = MyBeanUtil.copyList(coupons, CouponVO.class);
        for (CouponVO couponVO : couponVOS) {
            if (couponVO.getCouponTotal() != 0) {
                int count = couponUserService.count(new QueryWrapper<CouponUser>()
                        .eq("coupon_id", couponVO.getCouponId()));
                if (count >= couponVO.getCouponTotal()) {
                    couponVO.setSaleOut(true);
                }
            }
        }
        return couponVOS;
    }

    @Override
    public List<MyCouponVO> selectMyCoupons(Long userId) {
        List<CouponUser> couponUsers = couponUserService.list(new QueryWrapper<CouponUser>()
                .eq("user_id", userId)
                .eq("status", 0));
        List<MyCouponVO> myCouponVOS = MyBeanUtil.copyList(couponUsers, MyCouponVO.class);
        List<Long> couponIds = couponUsers.stream().map(item -> item.getCouponId()).collect(Collectors.toList());
        if (!couponIds.isEmpty()) {
            List<Coupon> coupons = listByIds(couponIds);
            for (Coupon coupon : coupons) {
                for (MyCouponVO myCouponVO : myCouponVOS) {
                    if (coupon.getCouponId().equals(myCouponVO.getCouponId())) {
                        myCouponVO.setName(coupon.getName());
                        myCouponVO.setCouponDesc(coupon.getCouponDesc());
                        myCouponVO.setDiscount(coupon.getDiscount());
                        myCouponVO.setMin(coupon.getMin());
                        myCouponVO.setGoodsType(coupon.getGoodsType());
                        myCouponVO.setGoodsValue(coupon.getGoodsValue());
                    }
                }
            }
        }
        return myCouponVOS;
    }
}
