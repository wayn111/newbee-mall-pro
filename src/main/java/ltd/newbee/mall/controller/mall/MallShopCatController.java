package ltd.newbee.mall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.ShopCat;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.entity.vo.MyCouponVO;
import ltd.newbee.mall.core.entity.vo.ShopCatVO;
import ltd.newbee.mall.core.service.CouponService;
import ltd.newbee.mall.core.service.ShopCatService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MallShopCatController extends BaseController {

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private CouponService couponService;

    @ResponseBody
    @RequestMapping(value = "shopCart", method = {RequestMethod.POST, RequestMethod.PUT})
    public R save(@RequestBody ShopCat shopCat, HttpSession session) {
        shopCat.setUserId(((MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY)).getUserId());
        shopCatService.saveShopCat(shopCat);
        return R.success();
    }

    @ResponseBody
    @GetMapping("shopCart/getUserShopCartCount")
    public R getUserShopCartCount(HttpSession session) {
        Object attribute = session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        long count = 0;
        if (attribute == null) {
            return R.success().add("count", count);
        }
        MallUserVO mallUserVO = (MallUserVO) attribute;
        count = (shopCatService.count(new QueryWrapper<ShopCat>()
                .eq("user_id", mallUserVO.getUserId())));
        return R.success().add("count", count);
    }

    @ResponseBody
    @DeleteMapping("shopCart/{id}")
    public R delete(@PathVariable("id") Long shopCatId) {
        shopCatService.removeById(shopCatId);
        return R.success();
    }


    @GetMapping("shopCart")
    public String shopCart(HttpServletRequest request, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        int itemsTotal = 0;
        int priceTotal = 0;
        List<ShopCatVO> collect = shopCatService.getShopCatVOList(mallUserVO.getUserId());
        if (CollectionUtils.isNotEmpty(collect)) {
            itemsTotal = collect.size();
            for (ShopCatVO shopCatVO : collect) {
                priceTotal += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
            }
        }
        request.setAttribute("itemsTotal", itemsTotal);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", collect);
        return "mall/shop-cat";
    }

    @GetMapping("shopCart/settle")
    public String settle(HttpServletRequest request, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<ShopCat> cats = shopCatService.list(new QueryWrapper<ShopCat>().eq("user_id", mallUserVO.getUserId()));
        if (CollectionUtils.isEmpty(cats)) {
            return "shop-cart";
        }
        int priceTotal = 0;
        List<ShopCatVO> collect = shopCatService.getShopCatVOList(mallUserVO.getUserId());
        if (CollectionUtils.isNotEmpty(collect)) {
            for (ShopCatVO shopCatVO : collect) {
                priceTotal += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
            }
        }
        List<MyCouponVO> myCouponVOS = couponService.selectMyCoupons(collect, priceTotal, mallUserVO.getUserId());
        request.setAttribute("coupons", myCouponVOS);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", collect);
        return "mall/order-settle";
    }

}
