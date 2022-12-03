package ltd.newbee.mall.core.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import ltd.newbee.mall.core.dao.CouponDao;
import ltd.newbee.mall.core.dao.CouponUserDao;
import ltd.newbee.mall.core.entity.Coupon;
import ltd.newbee.mall.core.entity.CouponUser;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.vo.CouponVO;
import ltd.newbee.mall.core.entity.vo.MyCouponVO;
import ltd.newbee.mall.core.entity.vo.ShopCatVO;
import ltd.newbee.mall.core.service.CouponService;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.util.MyBeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponDao, Coupon> implements CouponService {

    private CouponDao couponDao;

    private CouponUserDao couponUserDao;

    private GoodsService goodsService;


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
                CouponUser couponUser = couponUserDao.selectOne(new QueryWrapper<CouponUser>()
                        .eq("user_id", userId)
                        .eq("coupon_id", couponVO.getCouponId())
                        .eq("coupon_id", couponVO.getCouponId()));
                if (Objects.nonNull(couponUser)) {
                    couponVO.setHasReceived(true);
                }
            }
            // 处理总数有限制的优惠卷
            if (couponVO.getCouponTotal() != 0) {
                Long count = couponUserDao.selectCount(new QueryWrapper<CouponUser>()
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
        List<CouponUser> couponUsers = couponUserDao.selectAvailableList(userId);
        List<MyCouponVO> myCouponVOS = MyBeanUtil.copyList(couponUsers, MyCouponVO.class);
        List<Long> couponIds = couponUsers.stream().map(CouponUser::getCouponId).collect(toList());
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
        long nowTime = System.currentTimeMillis();
        return myCouponVOS.stream().filter(item -> {
            // 判断有效期
            Date startTime = item.getStartTime();
            Date endTime = item.getEndTime();
            if (startTime == null || endTime == null || nowTime < startTime.getTime() || nowTime > endTime.getTime()) {
                return false;
            }
            // 判断使用条件
            boolean b = false;
            if (item.getMin() <= priceTotal) {
                if (item.getGoodsType() == 1) { // 指定分类可用
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).toList();
                    List<Long> goodsIds = collect.stream().map(ShopCatVO::getGoodsId).collect(toList());
                    List<Goods> goods = goodsService.listByIds(goodsIds);
                    List<Long> categoryIds = goods.stream().map(Goods::getGoodsCategoryId).toList();
                    for (Long categoryId : categoryIds) {
                        if (goodsValue.contains(categoryId)) {
                            b = true;
                            break;
                        }
                    }
                } else if (item.getGoodsType() == 2) { // 指定商品可用
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).toList();
                    List<Long> goodsIds = collect.stream().map(ShopCatVO::getGoodsId).toList();
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
        }).sorted(Comparator.comparingInt(MyCouponVO::getDiscount)).collect(toList());
    }

    @Override
    public void releaseCoupon(Long orderId) {
        List<CouponUser> couponUserList = couponUserDao.selectList(new QueryWrapper<CouponUser>().eq("order_id", orderId));
        if (CollectionUtils.isEmpty(couponUserList)) {
            return;
        }
        for (CouponUser couponUser : couponUserList) {
            couponUser.setStatus((byte) 0);
            couponUser.setUpdateTime(new Date());
            couponUserDao.updateById(couponUser);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateCoupon(Coupon coupon) {
        coupon.setUpdateTime(new Date());
        return updateById(coupon);
    }
}
