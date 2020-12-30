package ltd.newbee.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.entity.Goods;
import ltd.newbee.mall.entity.Seckill;
import ltd.newbee.mall.entity.ShopCat;
import ltd.newbee.mall.entity.vo.MallUserVO;
import ltd.newbee.mall.service.GoodsService;
import ltd.newbee.mall.service.SeckillService;
import ltd.newbee.mall.service.ShopCatService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("seckill")
public class MallSeckillController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ShopCatService shopCatService;

    @GetMapping("time/now")
    public R getTimeNow() {
        return R.success().add("now", new Date());
    }

    @GetMapping("list")
    public String list(HttpServletRequest request, HttpSession session) {
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
        return "mall/seckill-list";
    }


    @GetMapping("detail/{seckillId}")
    public String detail(@PathVariable("seckillId") Long seckillId, HttpServletRequest request, HttpSession session) {
        Seckill seckill = seckillService.getById(seckillId);
        Long goodsId = seckill.getGoodsId();
        // 查询购物车中是否有该商品
        ShopCat shopCat = shopCatService.getOne(new QueryWrapper<ShopCat>()
                .eq("user_id", ((MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY)).getUserId())
                .eq("goods_id", goodsId));
        request.setAttribute("goodsCount", 0);
        if (Objects.nonNull(shopCat)) {
            request.setAttribute("cartItemId", shopCat.getCartItemId());
            request.setAttribute("goodsCount", shopCat.getGoodsCount());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        map.put("seckillPrice", seckill.getSeckillPrice());
        map.put("startDate", seckill.getSeckillBegin().getTime());
        map.put("endDate", seckill.getSeckillEnd().getTime());
        Goods goods = goodsService.getById(seckill.getGoodsId());
        map.put("goodsName", goods.getGoodsName());
        map.put("goodsIntro", goods.getGoodsIntro());
        map.put("originalPrice", goods.getOriginalPrice());
        map.put("goodsCoverImg", goods.getGoodsCoverImg());
        map.put("goodsIntro", goods.getGoodsIntro());
        map.put("goodsDetailContent", goods.getGoodsDetailContent());
        long now = System.currentTimeMillis();
        long startAt = seckill.getSeckillBegin().getTime();
        long endAt = seckill.getSeckillEnd().getTime();
        int miaoshaStatus;
        int remainSeconds;
        if (now < startAt) {// 秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) {// 秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else {// 秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        request.setAttribute("goodsDetail", map);
        request.setAttribute("seckillStatus", miaoshaStatus);
        request.setAttribute("remainSeconds", remainSeconds);
        return "mall/seckill-detail";
    }
}
