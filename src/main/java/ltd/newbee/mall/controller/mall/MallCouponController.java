package ltd.newbee.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.vo.CouponVO;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.entity.CouponUser;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.service.CouponService;
import ltd.newbee.mall.service.CouponUserService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Controller
public class MallCouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUserService couponUserService;

    @GetMapping("coupon")
    public String index(HttpServletRequest request) {
        List<CouponVO> coupons = couponService.selectAvailableCoupon();
        request.setAttribute("coupons", coupons);
        return "mall/coupon";
    }

    @ResponseBody
    @PostMapping("coupon/{couponId}")
    public R save(@PathVariable Long couponId, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Coupon coupon = couponService.getById(couponId);
        if (coupon.getCouponLimit() != 0) {
            int count = couponUserService.count(new QueryWrapper<CouponUser>()
                    .eq("user_id", mallUserVO.getUserId())
                    .eq("coupon_id", coupon.getCouponId()));
            if (count != 0) {
                throw new BusinessException("优惠卷已经领过了,无法再次领取！");
            }
        }
        if (coupon.getCouponTotal() != 0) {
            int count = couponUserService.count(new QueryWrapper<CouponUser>()
                    .eq("coupon_id", coupon.getCouponId()));
            if (count >= coupon.getCouponTotal()) {
                throw new BusinessException("优惠卷已经领完了！");
            }
        }
        CouponUser couponUser = new CouponUser();
        couponUser.setUserId(mallUserVO.getUserId());
        couponUser.setCouponId(coupon.getCouponId());
        LocalDate startLocalDate = LocalDate.now();
        LocalDate endLocalDate = startLocalDate.plusDays(coupon.getDays());
        ZoneId zone = ZoneId.systemDefault();
        Date startDate = Date.from(startLocalDate.atStartOfDay().atZone(zone).toInstant());
        Date endDate = Date.from(endLocalDate.atStartOfDay().atZone(zone).toInstant());
        couponUser.setStartTime(startDate);
        couponUser.setEndTime(endDate);
        couponUser.setCreateTime(new Date());
        couponUserService.save(couponUser);
        return R.success();
    }
}
