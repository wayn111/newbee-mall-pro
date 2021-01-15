package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.dao.OrderDao;
import ltd.newbee.mall.core.entity.*;
import ltd.newbee.mall.core.entity.vo.*;
import ltd.newbee.mall.core.service.*;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.task.OrderUnPaidTask;
import ltd.newbee.mall.task.TaskService;
import ltd.newbee.mall.util.NumberUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderDao, Order> implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUserService couponUserService;

    @Autowired
    private SeckillSuccessService seckillSuccessService;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RedisCache redisCache;

    @Override
    public IPage<OrderListVO> selectMyOrderPage(Page<OrderListVO> page, Order order) {
        return orderDao.selectListVOPage(page, order);
    }

    @Override
    public IPage<Order> selectPage(Page<Order> page, OrderVO orderVO) {
        return orderDao.selectListPage(page, orderVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveOrder(MallUserVO mallUserVO, Long couponUserId, List<ShopCatVO> shopcatVOList) {
        List<Long> goodsIdList = shopcatVOList.stream().map(ShopCatVO::getGoodsId).collect(Collectors.toList());
        List<Long> cartItemIdList = shopcatVOList.stream().map(ShopCatVO::getCartItemId).collect(Collectors.toList());
        List<Goods> goods = goodsService.listByIds(goodsIdList);
        // 检查是否包含已下架商品
        List<Goods> collect = goods.stream().filter(goods1 -> goods1.getGoodsSellStatus() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new BusinessException(collect.get(0).getGoodsName() + "已下架，无法生成订单");
        }
        Map<Long, Goods> goodsMap = goods.stream().collect(Collectors.toMap(Goods::getGoodsId, goods1 -> goods1));
        for (ShopCatVO shopCatVO : shopcatVOList) {
            // 查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!goodsMap.containsKey(shopCatVO.getGoodsId())) {
                throw new BusinessException("购物车中商品数据异常");
            }
            // 存在数量大于库存的情况，直接返回错误提醒
            if (shopCatVO.getGoodsCount() > goodsMap.get(shopCatVO.getGoodsId()).getStockNum()) {
                throw new BusinessException("购物车中:" + goodsMap.get(shopCatVO.getGoodsId()).getGoodsName() + " 库存不足");
            }
        }
        if (CollectionUtils.isEmpty(goodsIdList)
                || CollectionUtils.isEmpty(cartItemIdList)
                || CollectionUtils.isEmpty(goods)) {
            throw new BusinessException("结算异常");
        }
        // 删除购物项
        if (!shopCatService.removeByIds(cartItemIdList)) {
            throw new BusinessException("删除购物车异常");
        }
        List<Goods> goodsList = shopcatVOList.stream().map(shopCatVO -> {
            Goods goods1 = new Goods();
            goods1.setGoodsId(shopCatVO.getGoodsId());
            Integer stockNum = goodsMap.get(shopCatVO.getGoodsId()).getStockNum();
            goods1.setStockNum(stockNum - shopCatVO.getGoodsCount());
            return goods1;
        }).collect(Collectors.toList());
        // 更新商品库存
        if (!goodsService.updateBatchById(goodsList)) {
            throw new BusinessException("更新商品库存异常");
        }
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        int priceTotal = 0;
        for (ShopCatVO shopCatVO : shopcatVOList) {
            priceTotal += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
        }
        // 如果使用了优惠卷
        if (couponUserId != null) {
            CouponUser couponUser = couponUserService.getById(couponUserId);
            Coupon coupon = couponService.getById(couponUser.getCouponId());
            priceTotal -= coupon.getDiscount();
        }
        // 总价异常
        if (priceTotal <= 0) {
            throw new BusinessException("订单价格异常");
        }
        // 保存订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setTotalPrice(priceTotal);
        order.setUserId(mallUserVO.getUserId());
        order.setUserAddress(mallUserVO.getAddress());
        //todo 订单body字段，用来作为生成支付单描述信息，暂时未接入第三方支付接口，故该字段暂时设为空字符串
        String extraInfo = "";
        order.setExtraInfo(extraInfo);
        // 生成订单项并保存订单项纪录
        if (!save(order)) {
            throw new BusinessException("保存订单异常");
        }
        Long orderId = order.getOrderId();
        // 生成所有的订单项快照，并保存至数据库
        List<OrderItem> orderItems = shopcatVOList.stream().map(shopCatVO -> {
            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(shopCatVO, orderItem);
            orderItem.setOrderId(orderId);
            return orderItem;
        }).collect(Collectors.toList());
        // 如果使用了优惠卷，则更新优惠卷状态
        if (couponUserId != null) {
            CouponUser couponUser = new CouponUser();
            couponUser.setCouponUserId(couponUserId);
            couponUser.setStatus((byte) 1);
            couponUser.setUsedTime(new Date());
            couponUser.setOrderId(order.getOrderId());
            couponUserService.updateById(couponUser);
        }
        if (!orderItemService.saveBatch(orderItems)) {
            throw new BusinessException("保存订单内部异常");
        }
        // 订单支付超期任务
        taskService.addTask(new OrderUnPaidTask(orderId));
        // 所有操作成功后，将订单号返回
        return orderNo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String seckillSaveOrder(Long seckillSuccessId, MallUserVO userVO) {
        SeckillSuccess seckillSuccess = seckillSuccessService.getById(seckillSuccessId);
        if (!seckillSuccess.getUserId().equals(userVO.getUserId())) {
            throw new BusinessException("当前登陆用户与抢购秒杀商品的用户不匹配");
        }
        Long seckillId = seckillSuccess.getSeckillId();
        // 更新秒杀商品库存
        Long stock = redisCache.luaDecrement(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            throw new BusinessException("秒杀商品已售空");
        }
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (seckill == null) {
            seckill = seckillService.getById(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
        }
        // 判断秒杀商品是否再有效期内
        long beginTime = seckill.getSeckillBegin().getTime();
        long endTime = seckill.getSeckillEnd().getTime();
        Date now = new Date();
        long nowTime = now.getTime();
        if (nowTime < beginTime) {
            throw new BusinessException("秒杀未开启");
        } else if (nowTime > endTime) {
            throw new BusinessException("秒杀已结束");
        }
        // 减库存
        if (!seckillService.reduceStock(seckillId, now.getTime() / 1000)) {
            throw new BusinessException("秒杀商品减库存失败");
        }
        Long goodsId = seckill.getGoodsId();
        Goods goods = goodsService.getById(goodsId);
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
        if (!save(order)) {
            throw new BusinessException("生成订单内部异常");
        }
        // 记录购买过的用户
        redisCache.setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userVO.getUserId());
        long endExpireTime = endTime / 1000;
        long nowExpireTime = nowTime / 1000;
        redisCache.expire(Constants.SECKILL_SUCCESS_USER_ID + seckillId, endExpireTime - nowExpireTime, TimeUnit.SECONDS);

        // 保存订单商品项
        OrderItem orderItem = new OrderItem();
        Long orderId = order.getOrderId();
        orderItem.setOrderId(orderId);
        orderItem.setSeckillId(seckillId);
        orderItem.setGoodsId(goods.getGoodsId());
        orderItem.setGoodsCoverImg(goods.getGoodsCoverImg());
        orderItem.setGoodsName(goods.getGoodsName());
        orderItem.setGoodsCount(1);
        orderItem.setSellingPrice(seckill.getSeckillPrice());
        if (!orderItemService.save(orderItem)) {
            throw new BusinessException("生成订单内部异常");
        }
        // 秒杀订单1分钟未支付超期任务
        taskService.addTask(new OrderUnPaidTask(orderId, 1 * 60 * 1000));
        return orderNo;
    }

    @Override
    public List<DayTransactionAmountVO> countMallTransactionAmount(Integer dayNum) {
        if (dayNum < 0) {
            return Collections.emptyList();
        }
        return orderDao.countMallTransactionAmount(dayNum);
    }
}
