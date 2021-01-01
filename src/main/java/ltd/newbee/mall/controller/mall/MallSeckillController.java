package ltd.newbee.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import ltd.newbee.mall.base.BaseController;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.dao.SeckillDao;
import ltd.newbee.mall.entity.*;
import ltd.newbee.mall.entity.vo.ExposerVO;
import ltd.newbee.mall.entity.vo.MallUserVO;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.service.*;
import ltd.newbee.mall.util.NumberUtil;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.security.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("seckill")
public class MallSeckillController extends BaseController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private SeckillSuccessService seckillSuccessService;

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @ResponseBody
    @GetMapping("time/now")
    public R getTimeNow() {
        return R.success().add("now", new Date().getTime());
    }

    @ResponseBody
    @PostMapping("{seckillId}/exposer")
    public R exposerUrl(@PathVariable Long seckillId) {
        Seckill seckill = seckillService.getById(seckillId);
        Date startTime = seckill.getSeckillBegin();
        Date endTime = seckill.getSeckillEnd();
        // 系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            ExposerVO exposerVO = new ExposerVO(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
            return R.success().add("exposer", exposerVO);
        }
        String md5 = Md5Utils.hash(seckillId);
        ExposerVO exposerVO = new ExposerVO(true, md5, seckillId);
        return R.success().add("exposer", exposerVO);
    }

    @PostMapping(value = "/{seckillId}/{md5}/execution")
    public String execute(@PathVariable("seckillId") Long seckillId,
                          @PathVariable("md5") String md5, HttpSession session) {
        if (md5 == null || !md5.equals(Md5Utils.hash(seckillId))) {
            throw new BusinessException("秒杀商品不存在");
        }
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Seckill seckill = seckillService.getById(seckillId);
        Goods goods = goodsService.getById(seckill.getGoodsId());
        int count = seckillSuccessService.count(new QueryWrapper<SeckillSuccess>()
                .eq("seckill_id", seckillId)
                .eq("user_id", userVO.getUserId()));
        if (count >= seckill.getLimitNum()) {
            throw new BusinessException("用户购买数量有已经超出秒杀限购数量");
        }
        if (seckill.getSeckillNum() <= 0) {
            throw new BusinessException("秒杀商品已售空");
        }
        // 执行秒杀逻辑：减库存 + 记录购买行为
        Date now = new Date();
        if (!seckillDao.reduceNumber(seckillId, now.getTime() / 1000)) {
            throw new BusinessException("秒杀商品减库存失败");
        }
        SeckillSuccess seckillSuccess = new SeckillSuccess();
        seckillSuccess.setSeckillId(seckillId);
        seckillSuccess.setUserId(userVO.getUserId());
        if (!seckillSuccessService.save(seckillSuccess)) {
            throw new BusinessException("保存用户秒杀商品失败");
        }
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        // 保存订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setTotalPrice(seckill.getSeckillPrice());
        order.setUserId(userVO.getUserId());
        order.setUserAddress(userVO.getAddress());
        String extraInfo = "";
        order.setExtraInfo(extraInfo);
        if (!orderService.save(order)) {
            throw new BusinessException("生成订单内部异常");
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setGoodsId(goods.getGoodsId());
        orderItem.setGoodsCoverImg(goods.getGoodsCoverImg());
        orderItem.setGoodsName(goods.getGoodsName());
        orderItem.setGoodsCount(1);
        orderItem.setSellingPrice(seckill.getSeckillPrice());
        if (!orderItemService.save(orderItem)) {
            throw new BusinessException("生成订单内部异常");
        }
        return redirectTo("/orders/" + orderNo);
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
        request.setAttribute("cartItemId", 0);
        request.setAttribute("goodsCount", 0);
        // 查询购物车中是否有该商品
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (userVO != null) {
            ShopCat shopCat = shopCatService.getOne(new QueryWrapper<ShopCat>()
                    .eq("user_id", userVO.getUserId())
                    .eq("goods_id", goodsId));
            request.setAttribute("goodsCount", 0);
            if (Objects.nonNull(shopCat)) {
                request.setAttribute("cartItemId", shopCat.getCartItemId());
                request.setAttribute("goodsCount", shopCat.getGoodsCount());
            }
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
        request.setAttribute("seckillId", seckill.getSeckillId());
        request.setAttribute("seckillStatus", miaoshaStatus);
        request.setAttribute("remainSeconds", remainSeconds);
        return "mall/seckill-detail";
    }
}
