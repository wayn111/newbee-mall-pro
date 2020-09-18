package ltd.newbee.mall.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.controller.vo.*;
import ltd.newbee.mall.dao.OrderDao;
import ltd.newbee.mall.entity.*;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.service.*;
import ltd.newbee.mall.util.NumberUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
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

    @Override
    public IPage selectMyOrderPage(Page<OrderListVO> page, Order order) {
        return orderDao.selectListVOPage(page, order);
    }

    @Override
    public IPage selectPage(Page<Order> page, OrderVO orderVO) {
        return orderDao.selectListPage(page, orderVO);
    }

    @Transactional
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
        if (CollectionUtils.isNotEmpty(goodsIdList)
                && CollectionUtils.isNotEmpty(cartItemIdList)
                && CollectionUtils.isNotEmpty(goods)) {
            // 删除购物项
            if (shopCatService.removeByIds(cartItemIdList)) {
                List<Goods> collect1 = shopcatVOList.stream().map(shopCatVO -> {
                    Goods goods1 = new Goods();
                    goods1.setGoodsId(shopCatVO.getGoodsId());
                    Integer stockNum = goodsMap.get(shopCatVO.getGoodsId()).getStockNum();
                    goods1.setStockNum(stockNum - shopCatVO.getGoodsCount());
                    return goods1;
                }).collect(Collectors.toList());
                // 更新商品库存
                if (goodsService.updateBatchById(collect1)) {
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
                    if (save(order)) {
                        // 生成所有的订单项快照，并保存至数据库
                        List<OrderItem> orderItems = shopcatVOList.stream().map(shopCatVO -> {
                            OrderItem orderItem = new OrderItem();
                            BeanUtils.copyProperties(shopCatVO, orderItem);
                            orderItem.setOrderId(order.getOrderId());
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
                        if (orderItemService.saveBatch(orderItems)) {
                            // 所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
                            return orderNo;
                        }
                    }
                }
            }
        }
        throw new BusinessException("结算异常");
    }

    @Override
    public List<DayTransactionAmountVO> countMallTransactionAmount() {
        return orderDao.countMallTransactionAmount();
    }
}
