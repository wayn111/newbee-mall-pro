package ltd.newbee.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ltd.newbee.mall.annotation.Limit;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.Seckill;
import ltd.newbee.mall.core.entity.SeckillSuccess;
import ltd.newbee.mall.core.entity.vo.ExposerVO;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.entity.vo.SeckillSuccessVO;
import ltd.newbee.mall.core.entity.vo.ShopCatVO;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.core.service.SeckillService;
import ltd.newbee.mall.core.service.SeckillSuccessService;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.security.Md5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping("seckill")
public class MallSeckillController extends BaseController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SeckillSuccessService seckillSuccessService;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    private RedisCache redisCache;

    @ResponseBody
    @GetMapping("time/now")
    public R getTimeNow() {
        return R.success().add("now", System.currentTimeMillis());
    }

    @ResponseBody
    @PostMapping("{seckillId}/exposer")
    public R exposerUrl(@PathVariable Long seckillId) {
        ExposerVO exposerVO = seckillService.exposerUrl(seckillId);
        return R.success().add("exposer", exposerVO);
    }

    @ResponseBody
    // 接口限流注解
    @Limit(key = "seckill", period = 1, count = 200, name = "执行秒杀限制", prefix = Constants.CACHE_PREFIX)
    @PostMapping(value = "/{seckillId}/{md5}/execution")
    public R execute(@PathVariable Long seckillId,
                     @PathVariable String md5, HttpSession session) {
        if (md5 == null || !md5.equals(Md5Utils.hash(seckillId))) {
            throw new BusinessException("秒杀商品不存在");
        }
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        SeckillSuccessVO seckillSuccessVO = seckillService.executeSeckillFinal(seckillId, userVO);
        return R.success().add("seckillSuccess", seckillSuccessVO);
    }

    @GetMapping("/{seckillSuccessId}/{md5}/settle")
    public String settle(@PathVariable Long seckillSuccessId,
                         @PathVariable String md5,
                         HttpServletRequest request,
                         HttpSession session) {
        if (md5 == null || !md5.equals(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_EXECUTE_SALT))) {
            throw new BusinessException("秒杀商品结算不合法");
        }
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        SeckillSuccess seckillSuccess = seckillSuccessService.getById(seckillSuccessId);
        if (!seckillSuccess.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("当前登陆用户与抢购秒杀商品的用户不匹配");
        }
        Long seckillId = seckillSuccess.getSeckillId();
        Seckill seckill = seckillService.getById(seckillId);
        ShopCatVO shopCatVO = new ShopCatVO();
        Long goodsId = seckill.getGoodsId();
        Goods goods = goodsService.getById(goodsId);
        shopCatVO.setGoodsId(goodsId);
        shopCatVO.setGoodsName(goods.getGoodsName());
        shopCatVO.setGoodsCoverImg(goods.getGoodsCoverImg());
        shopCatVO.setGoodsCount(1);
        shopCatVO.setSellingPrice(seckill.getSeckillPrice());
        request.setAttribute("isSeckillSettle", true);
        request.setAttribute("seckillSuccessId", seckillSuccessId);
        request.setAttribute("seckillSecretKey", Md5Utils.hash(seckillSuccessId + Constants.SECKILL_ORDER_SALT));
        request.setAttribute("priceTotal", seckill.getSeckillPrice());
        request.setAttribute("myShoppingCartItems", Collections.singletonList(shopCatVO));
        return "mall/order-settle";
    }

    @GetMapping("list")
    @ResponseBody
    public String list(HttpServletRequest request,
                       HttpServletResponse response,
                       Model model) {
        // 判断缓存中是否有当前秒杀商品列表页面
        String html = redisCache.getCacheObject(Constants.SECKILL_GOODS_LIST_HTML);
        if (StringUtils.isNotBlank(html)) {
            return html;
        }
        List<Seckill> seckillList = seckillService.list(new QueryWrapper<Seckill>().eq("status", 1).orderByDesc("seckill_rank"));
        List<Map<String, Object>> list = seckillList.stream().map(seckill -> {
            Map<String, Object> map = new HashMap<>();
            map.put("seckillId", seckill.getSeckillId());
            map.put("goodsId", seckill.getGoodsId());
            map.put("seckillPrice", seckill.getSeckillPrice());
            Goods goods = goodsService.getById(seckill.getGoodsId());
            map.put("goodsName", goods.getGoodsName());
            map.put("originalPrice", goods.getOriginalPrice());
            map.put("goodsCoverImg", goods.getGoodsCoverImg());
            map.put("goodsIntro", goods.getGoodsIntro());
            return map;
        }).collect(Collectors.toList());
        request.setAttribute("seckillList", list);
        // 缓存秒杀商品列表页
        JakartaServletWebApplication jakartaServletWebApplication = JakartaServletWebApplication.buildApplication(request.getServletContext());
        WebContext ctx = new WebContext(jakartaServletWebApplication.buildExchange(request, response), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("mall/seckill-list", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisCache.setCacheObject(Constants.SECKILL_GOODS_LIST_HTML, html, 100, TimeUnit.HOURS);
        }
        return html;
    }


    @GetMapping("detail/{seckillId}")
    @ResponseBody
    public String detail(@PathVariable("seckillId") Long seckillId,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         Model model) {
        // 判断缓存中是否有当前秒杀商品详情页面
        String html = redisCache.getCacheObject(Constants.SECKILL_GOODS_DETAIL_HTML + seckillId);
        if (StringUtils.isNotBlank(html)) {
            return html;
        }
        Seckill seckill = seckillService.getById(seckillId);
        Long goodsId = seckill.getGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        map.put("seckillPrice", seckill.getSeckillPrice());
        map.put("startDate", seckill.getSeckillBegin().getTime());
        map.put("endDate", seckill.getSeckillEnd().getTime());
        Goods goods = goodsService.getById(seckill.getGoodsId());
        map.put("goodsName", goods.getGoodsName());
        map.put("originalPrice", goods.getOriginalPrice());
        map.put("goodsCoverImg", goods.getGoodsCoverImg());
        map.put("goodsIntro", goods.getGoodsIntro());
        map.put("goodsDetailContent", goods.getGoodsDetailContent());
        long now = System.currentTimeMillis();
        long startAt = seckill.getSeckillBegin().getTime();
        long endAt = seckill.getSeckillEnd().getTime();
        int miaoshaStatus;
        int remainSeconds;
        if (now < startAt) {
            // 秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) {
            // 秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else {
            // 秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        request.setAttribute("goodsDetail", map);
        request.setAttribute("seckillId", seckill.getSeckillId());
        request.setAttribute("seckillStatus", miaoshaStatus);
        request.setAttribute("remainSeconds", remainSeconds);
        // 缓存秒杀商品详情页
        JakartaServletWebApplication jakartaServletWebApplication = JakartaServletWebApplication.buildApplication(request.getServletContext());
        WebContext ctx = new WebContext(jakartaServletWebApplication.buildExchange(request, response), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("mall/seckill-detail", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisCache.setCacheObject(Constants.SECKILL_GOODS_DETAIL_HTML + seckillId, html, 30, TimeUnit.MINUTES);
        }
        return html;
    }
}
