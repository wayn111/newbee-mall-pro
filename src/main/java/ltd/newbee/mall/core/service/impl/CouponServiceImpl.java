package ltd.newbee.mall.core.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.core.dao.CouponDao;
import ltd.newbee.mall.core.entity.Coupon;
import ltd.newbee.mall.core.entity.CouponUser;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.vo.CouponVO;
import ltd.newbee.mall.core.entity.vo.MyCouponVO;
import ltd.newbee.mall.core.entity.vo.ShopCatVO;
import ltd.newbee.mall.core.service.CouponService;
import ltd.newbee.mall.core.service.CouponUserService;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.util.MyBeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponDao, Coupon> implements CouponService {

    private CouponDao couponDao;

    private CouponUserService couponUserService;

    private GoodsService goodsService;

    @Autowired
    public void setCouponDao(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    @Lazy
    @Autowired
    public void setCouponUserService(CouponUserService couponUserService) {
        this.couponUserService = couponUserService;
    }

    @Autowired
    public void setGoodsService(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    public CouponDao getCouponDao() {
        return couponDao;
    }

    public CouponUserService getCouponUserService() {
        return couponUserService;
    }

    public GoodsService getGoodsService() {
        return goodsService;
    }

    @Override
    public IPage<Coupon> selectPage(Page<Coupon> page, CouponVO coupon) {
        return couponDao.selectListPage(page, coupon);
    }

    @Override
    public List<CouponVO> selectAvailableCoupon(Long userId) {
        List<Coupon> coupons = couponDao.selectAvailableCoupon();
        List<CouponVO> couponVOS = MyBeanUtil.copyList(coupons, CouponVO.class);
        for (CouponVO couponVO : couponVOS) {
            couponVO.setHasReceived(false);
            // 处理领取数量有限制的优惠卷
            if (userId != null && couponVO.getCouponLimit() == 1) {
                CouponUser couponUser = couponUserService.getOne(new QueryWrapper<CouponUser>()
                        .eq("user_id", userId)
                        .eq("coupon_id", couponVO.getCouponId())
                        .eq("coupon_id", couponVO.getCouponId()));
                if (Objects.nonNull(couponUser)) {
                    couponVO.setHasReceived(true);
                }
            }
            // 处理总数有限制的优惠卷
            if (couponVO.getCouponTotal() != 0) {
                long count = couponUserService.count(new QueryWrapper<CouponUser>()
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
                if (item.getGoodsType() == 1) { // 指定分类可用
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
                } else if (item.getGoodsType() == 2) { // 指定商品可用
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList());
                    List<Long> goodsIds = collect.stream().map(ShopCatVO::getGoodsId).collect(Collectors.toList());
                    for (Long goodsId : goodsIds) {
                        if (goodsValue.contains(goodsId)) {
                            b = true;
                            break;
                        }
                    }
                } else { // 全场通用
                    b = true;
                }
            }
            return b;
        }).sorted(Comparator.comparingInt(MyCouponVO::getDiscount)).collect(Collectors.toList());
    }

    @Override
    public void releaseCoupon(Long orderId) {
        List<CouponUser> couponUserList = couponUserService.list(new QueryWrapper<CouponUser>().eq("order_id", orderId));
        if (CollectionUtils.isEmpty(couponUserList)) {
            return;
        }
        for (CouponUser couponUser : couponUserList) {
            couponUser.setStatus((byte) 0);
            couponUser.setUpdateTime(new Date());
            couponUserService.updateById(couponUser);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateCoupon(Coupon coupon) {
        //  修改优惠劵状态时，还要修改用户已经领过优惠劵的状态
        if (coupon.getStatus() == 2) {
            couponUserService.update()
                    .eq("coupon_id", coupon.getCouponId())
                    .eq("status", 0)
                    .set("status", 3)
                    .update();
        } else if (coupon.getStatus() == 0) {
            couponUserService.update()
                    .eq("coupon_id", coupon.getCouponId())
                    .eq("status", 3)
                    .set("status", 0)
                    .update();
        }
        coupon.setUpdateTime(new Date());
        return updateById(coupon);
    }
}
