package ltd.newbee.mall.controller.mall;


import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.vo.CouponVO;
import ltd.newbee.mall.controller.vo.MallUserVO;
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
        couponUserService.saveCouponUser(couponId, mallUserVO.getUserId());
        return R.success();
    }
}
