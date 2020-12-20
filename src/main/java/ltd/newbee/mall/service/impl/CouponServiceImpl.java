package ltd.newbee.mall.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.dao.CouponDao;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.entity.CouponUser;
import ltd.newbee.mall.entity.Goods;
import ltd.newbee.mall.entity.vo.CouponVO;
import ltd.newbee.mall.entity.vo.MyCouponVO;
import ltd.newbee.mall.entity.vo.ShopCatVO;
import ltd.newbee.mall.service.CouponService;
import ltd.newbee.mall.service.CouponUserService;
import ltd.newbee.mall.service.GoodsService;
import ltd.newbee.mall.util.MyBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponDao, Coupon> implements CouponService {

    @Autowired
    private CouponDao couponDao;

    @Autowired
    private CouponUserService couponUserService;

    @Autowired
    private GoodsService goodsService;

    @Override
    public IPage selectPage(Page<Coupon> page, CouponVO coupon) {
        return couponDao.selectListPage(page, coupon);
    }

    @Override
    public List<CouponVO> selectAvailableCoupon(Long userId) {
        List<Coupon> coupons = couponDao.selectAvailableCoupon();
        List<CouponVO> couponVOS = MyBeanUtil.copyList(coupons, CouponVO.class);
        for (CouponVO couponVO : couponVOS) {
            couponVO.setHasReceived(false);
            if (userId != null) {
                CouponUser couponUser = couponUserService.getOne(new QueryWrapper<CouponUser>()
                        .eq("user_id", userId)
                        .eq("coupon_id", couponVO.getCouponId()));
                if (Objects.nonNull(couponUser)) {
                    couponVO.setHasReceived(true);
                }
            }
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
    public List<MyCouponVO> selectMyCoupons(List<ShopCatVO> collect, int priceTotal, Long userId) {
        List<CouponUser> couponUsers = couponUserService.list(new QueryWrapper<CouponUser>()
                .eq("user_id", userId)
                .eq("status", 0));
        List<MyCouponVO> myCouponVOS = MyBeanUtil.copyList(couponUsers, MyCouponVO.class);
        List<Long> couponIds = couponUsers.stream().map(CouponUser::getCouponId).collect(Collectors.toList());
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
        return myCouponVOS.stream().filter(item -> {
            boolean b = false;
            if (item.getMin() <= priceTotal) {
                if (item.getGoodsType() == 1) {
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList());
                    List<Long> goodsIds = collect.stream().map(ShopCatVO::getGoodsId).collect(Collectors.toList());
                    List<Goods> goods = goodsService.listByIds(goodsIds);
                    List<Long> categoryIds = goods.stream().map(Goods::getGoodsCategoryId).collect(Collectors.toList());
                    for (Long categoryId : categoryIds) {
                        if (goodsValue.contains(categoryId)) {
                            b = true;
                            break;
                        }
                    }
                } else if (item.getGoodsType() == 2) {
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList());
                    List<Long> goodsIds = collect.stream().map(ShopCatVO::getGoodsId).collect(Collectors.toList());
                    for (Long goodsId : goodsIds) {
                        if (goodsValue.contains(goodsId)) {
                            b = true;
                            break;
                        }
                    }
                } else {
                    b = true;
                }
            }
            return b;
        }).sorted(Comparator.comparingInt(MyCouponVO::getDiscount)).collect(Collectors.toList());
    }
}
