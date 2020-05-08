package ltd.newbee.mall.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.dao.CouponDao;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponDao, Coupon> implements CouponService {

    @Autowired
    private CouponDao couponDao;

    @Override
    public IPage selectPage(Page<Coupon> page, Coupon coupon) {
        return couponDao.selectListPage(page, coupon);
    }
}
