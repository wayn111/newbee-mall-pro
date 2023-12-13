package ltd.newbee.mall.core.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.dao.OrderDao;
import ltd.newbee.mall.core.entity.*;
import ltd.newbee.mall.core.entity.vo.*;
import ltd.newbee.mall.core.service.*;
import ltd.newbee.mall.event.OrderEvent;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.task.TaskService;
import ltd.newbee.mall.util.NumberUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ltd.newbee.mall.mq.RabbitMQConstant.EXCHANGE_NAME;
import static ltd.newbee.mall.mq.RabbitMQConstant.ROUTE_NAME;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderDao, Order> implements OrderService {

    private OrderDao orderDao;

    private GoodsService goodsService;

    private ShopCatService shopCatService;

    private OrderItemService orderItemService;

    private CouponService couponService;

    private CouponUserService couponUserService;

    private SeckillSuccessService seckillSuccessService;

    private SeckillService seckillService;

    private TaskService taskService;

    private RedisCache redisCache;

    private ApplicationEventPublisher applicationEventPublisher;
    private RabbitTemplate rabbitTemplate;

    @Override
    public IPage<OrderListVO> selectMyOrderPage(Page<OrderListVO> page, Order order) {
        return orderDao.selectListVOPage(page, order);
    }

    @Override
    public IPage<Order> selectPage(Page<Order> page, OrderVO orderVO) {
        return orderDao.selectListPage(page, orderVO);
    }

    @Override
    public String saveOrder(MallUserVO mallUserVO, Long couponUserId, List<ShopCatVO> shopcatVOList) {
        List<Long> goodsIdList = shopcatVOList.stream().map(ShopCatVO::getGoodsId).collect(Collectors.toList());
        List<Long> cartItemIdList = shopcatVOList.stream().map(ShopCatVO::getCartItemId).collect(Collectors.toList());
        // 检查是否包含已下架商品
        saveOrderCheck(shopcatVOList, goodsIdList, cartItemIdList, couponUserId);
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        // 生成订单、删除购物车、扣减库存
        saveOrder(mallUserVO, couponUserId, shopcatVOList, orderNo);
        // 所有操作成功后，将订单号返回
        return orderNo;
    }

    private void saveOrder(MallUserVO mallUserVO, Long couponUserId, List<ShopCatVO> shopcatVOList, String orderNo) {
        applicationEventPublisher.publishEvent(new OrderEvent(orderNo, mallUserVO, couponUserId, shopcatVOList));
    }

    /**
     * 下单检查
     *
     * @param shopcatVOList
     * @param goodsIdList
     * @param cartItemIdList
     * @param couponUserId
     */
    private void saveOrderCheck(List<ShopCatVO> shopcatVOList, List<Long> goodsIdList, List<Long> cartItemIdList, Long couponUserId) {
        List<Goods> goods = goodsService.listByIds(goodsIdList);
        List<Goods> collect = goods.stream().filter(goods1 -> goods1.getGoodsSellStatus() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new BusinessException(collect.getFirst().getGoodsName() + "已下架，无法生成订单");
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

        if (CollectionUtils.isEmpty(goodsIdList)
                || CollectionUtils.isEmpty(cartItemIdList)
                || CollectionUtils.isEmpty(goods)) {
            throw new BusinessException("结算异常");
        }
    }

    @Override
    public String seckillSaveOrder(Long seckillSuccessId, MallUserVO userVO) {
        // 秒杀订单参数检查
        Date now = new Date();
        Seckill seckill = seckillSaveOrderCheck(seckillSuccessId, userVO, now);
        long nowTime = now.getTime();
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        seckillSaveOrder(seckill, userVO, nowTime, orderNo);
        return orderNo;
    }

    private Seckill seckillSaveOrderCheck(Long seckillSuccessId, MallUserVO userVO, Date now) {
        SeckillSuccess seckillSuccess = seckillSuccessService.getById(seckillSuccessId);
        if (!seckillSuccess.getUserId().equals(userVO.getUserId())) {
            throw new BusinessException("当前登陆用户与抢购秒杀商品的用户不匹配");
        }
        Long seckillId = seckillSuccess.getSeckillId();
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (seckill == null) {
            seckill = seckillService.getById(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
        }
        // 更新秒杀商品库存
        Long stock = redisCache.luaDecrement(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            throw new BusinessException("秒杀商品已售空");
        }
        // 判断秒杀商品是否再有效期内
        long beginTime = seckill.getSeckillBegin().getTime();
        long endTime = seckill.getSeckillEnd().getTime();
        long nowTime = now.getTime();
        if (nowTime < beginTime) {
            throw new BusinessException("秒杀未开启");
        } else if (nowTime > endTime) {
            throw new BusinessException("秒杀已结束");
        }
        return seckill;
    }

    private void seckillSaveOrder(Seckill seckill, MallUserVO userVO, Long nowTime, String orderNo) {
        // 异步下单，发送消息到RabbitMQ
        String uid = IdUtil.fastSimpleUUID();
        CorrelationData correlationData = new CorrelationData(uid);
        Map<String, Object> map = new HashMap<>();
        map.put("seckill", seckill);
        map.put("userVO", userVO);
        map.put("nowTime", nowTime);
        map.put("orderNo", orderNo);
        try {
            Message message = MessageBuilder
                    .withBody(JSON.toJSONString(map).getBytes(Constants.UTF_ENCODING))
                    .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTE_NAME, message, correlationData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public List<DayTransactionAmountVO> countMallTransactionAmount(Integer dayNum) {
        if (dayNum < 0) {
            return Collections.emptyList();
        }
        return orderDao.countMallTransactionAmount(dayNum);
    }

    @Override
    public Order judgeOrderUserId(String orderNo, Long userId) {
        Order order = this.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        // 判断订单userId
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("当前订单用户异常");
        }
        return order;
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return this.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
    }
}
