package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Coupon;
import ltd.newbee.mall.core.entity.vo.CouponVO;
import ltd.newbee.mall.core.service.CouponService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("admin/coupon")
public class CouponManagerController extends BaseController {
    private static final String PREFIX = "admin/coupon";

    @Autowired
    private CouponService couponService;

    @GetMapping
    public String index(HttpServletRequest request) {
        request.setAttribute("configType", 3);
        request.setAttribute("path", "coupon");
        return PREFIX + "/coupon";
    }

    @ResponseBody
    @GetMapping("/list")
    public IPage<Coupon> list(CouponVO couponVO, HttpServletRequest request) {
        Page<Coupon> page = getPage(request);
        return couponService.selectPage(page, couponVO);
    }

    /**
     * 保存
     *
     * @param coupon
     * @return
     */
    @ResponseBody
    @PostMapping("/save")
    public R save(@RequestBody Coupon coupon) {
        coupon.setCreateTime(new Date());
        return R.result(couponService.save(coupon));
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @ResponseBody
    public R update(@RequestBody Coupon coupon) {
        return R.result(couponService.updateCoupon(coupon));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    @ResponseBody
    public R Info(@PathVariable("id") Long id) {
        return R.success().add("data", couponService.getById(id));
    }

    /**
     * 详情
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public R delete(@PathVariable("id") Long id) {
        return R.result(couponService.removeById(id));
    }
}
