package ltd.newbee.mall.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.dao.CouponUserDao;
import ltd.newbee.mall.entity.CouponUser;
import ltd.newbee.mall.service.CouponUserService;
import org.springframework.stereotype.Service;

@Service
public class CouponUserServiceImpl extends ServiceImpl<CouponUserDao, CouponUser> implements CouponUserService {
}
