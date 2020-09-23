package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.base.BaseController;
import ltd.newbee.mall.controller.vo.CouponVO;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.service.CouponService;
import ltd.newbee.mall.service.CouponUserService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@RequestMapping("admin/coupon")
public class CouponManagerController extends BaseController {
    private static final String PREFIX = "admin/coupon";

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUserService couponUserService;

    @GetMapping
    public String index(HttpServletRequest request) {
        request.setAttribute("configType", 3);
        return PREFIX + "/coupon";
    }

    @ResponseBody
    @GetMapping("/list")
    public IPage list(CouponVO couponVO, HttpServletRequest request) {
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
        couponService.save(coupon);
        return R.success();
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @ResponseBody
    public R update(@RequestBody Coupon coupon) {
        coupon.setUpdateTime(new Date());
        couponService.updateById(coupon);
        couponUserService.update();
        //  当修改状态为可用时，又要修改对应
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
        return R.success();
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
        couponService.removeById(id);
        return R.success();
    }
}
