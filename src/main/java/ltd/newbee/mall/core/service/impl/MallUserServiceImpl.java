package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.core.dao.MallUserDao;
import ltd.newbee.mall.core.entity.Coupon;
import ltd.newbee.mall.core.entity.CouponUser;
import ltd.newbee.mall.core.entity.MallUser;
import ltd.newbee.mall.core.service.CouponService;
import ltd.newbee.mall.core.service.CouponUserService;
import ltd.newbee.mall.core.service.MallUserService;
import ltd.newbee.mall.util.security.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MallUserServiceImpl extends ServiceImpl<MallUserDao, MallUser> implements MallUserService {

    @Autowired
    private MallUserDao mallUserDao;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUserService couponUserService;

    @Override
    public IPage<MallUser> selectPage(Page<MallUser> page, MallUser mallUser) {
        return mallUserDao.selectListPage(page, mallUser);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean register(String loginName, String password) {
        MallUser mallUser = new MallUser();
        mallUser.setLoginName(loginName);
        mallUser.setNickName(UUID.randomUUID().toString().substring(0, 5));
        mallUser.setPasswordMd5(Md5Utils.hash(password));
        // 添加注册赠卷
        List<Coupon> coupons = couponService.list(new QueryWrapper<Coupon>()
                .eq("coupon_type", 1));
        List<CouponUser> couponUserList = coupons.stream().map(coupon -> {
            CouponUser couponUser = new CouponUser();
            couponUser.setUserId(mallUser.getUserId());
            couponUser.setCouponId(coupon.getCouponId());
            return couponUser;
        }).collect(Collectors.toList());
        couponUserService.saveBatch(couponUserList);
        return save(mallUser);
    }
}
