package ltd.newbee.mall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import ltd.newbee.mall.base.BaseController;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.controller.vo.MyCouponVO;
import ltd.newbee.mall.controller.vo.ShopCatVO;
import ltd.newbee.mall.entity.Goods;
import ltd.newbee.mall.entity.ShopCat;
import ltd.newbee.mall.service.CouponService;
import ltd.newbee.mall.service.GoodsService;
import ltd.newbee.mall.service.ShopCatService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MallShopCatController extends BaseController {

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private GoodsService goodsService;

    @ResponseBody
    @RequestMapping(value = "shopCart", method = {RequestMethod.POST, RequestMethod.PUT})
    public R save(@RequestBody ShopCat shopCat, HttpSession session) {
        shopCat.setUserId(((MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY)).getUserId());
        shopCatService.saveShopCat(shopCat);
        return R.success();
    }

    @ResponseBody
    @DeleteMapping("shopCart/{id}")
    public R delete(@PathVariable("id") Long shopCatId) {
        shopCatService.removeById(shopCatId);
        return R.success();
    }


    @GetMapping("shopCart")
    public String save(HttpServletRequest request, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        int itemsTotal = 0;
        int priceTotal = 0;
        List<ShopCatVO> collect = shopCatService.getShopcatVOList(mallUserVO.getUserId());
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
        List<ShopCatVO> collect = shopCatService.getShopcatVOList(mallUserVO.getUserId());
        if (CollectionUtils.isNotEmpty(collect)) {
            for (ShopCatVO shopCatVO : collect) {
                priceTotal += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
            }
        }
        List<MyCouponVO> myCouponVOS = couponService.selectMyCoupons(mallUserVO.getUserId());
        int finalPriceTotal = priceTotal;
        List<MyCouponVO> finalMyCouponVOS = myCouponVOS.stream().filter(item -> {
            boolean b = false;
            if (item.getMin() <= finalPriceTotal) {
                if (item.getGoodsType() == 1) {
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
                } else if (item.getGoodsType() == 2) {
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList());
                    List<Long> goodsIds = collect.stream().map(ShopCatVO::getGoodsId).collect(Collectors.toList());
                    for (Long goodsId : goodsIds) {
                        if (goodsValue.contains(goodsId)) {
                            b = true;
                            break;
                        }
                    }
                } else {
                    b = true;
                }
            }
            return b;
        }).sorted(Comparator.comparingInt(MyCouponVO::getDiscount)).collect(Collectors.toList());
        request.setAttribute("coupons", finalMyCouponVOS);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", collect);
        return "mall/order-settle";
    }

}
