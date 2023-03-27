package ltd.newbee.mall.controller.mall;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.config.AlipayConfig;
import ltd.newbee.mall.config.NewbeeMallConfig;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Coupon;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.OrderItem;
import ltd.newbee.mall.core.entity.vo.*;
import ltd.newbee.mall.core.service.CouponUserService;
import ltd.newbee.mall.core.service.OrderItemService;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.core.service.ShopCatService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.enums.PayStatusEnum;
import ltd.newbee.mall.enums.PayTypeEnum;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.task.OrderUnPaidTask;
import ltd.newbee.mall.task.TaskService;
import ltd.newbee.mall.util.MyBeanUtil;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.security.Md5Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class MallOrderController extends BaseController {

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CouponUserService couponUserService;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private NewbeeMallConfig newbeeMallConfig;

    @ResponseBody
    @GetMapping("saveOrder")
    public R saveOrder(Long couponUserId, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<ShopCatVO> shopcatVOList = shopCatService.getShopCatVOList(mallUserVO.getUserId());
        // 购物车中无数据则跳转至错误页
        if (CollectionUtils.isEmpty(shopcatVOList)) {
            throw new BusinessException("购物车中无数据");
        }
        String orderNo = orderService.saveOrder(mallUserVO, couponUserId, shopcatVOList);
        return R.success().add("orderNo", orderNo);
    }

    @ResponseBody
    @GetMapping("seckillSaveOrder/{seckillSuccessId}/{seckillSecretKey}")
    public R seckillSaveOrder(@PathVariable Long seckillSuccessId,
                              @PathVariable String seckillSecretKey,
                              HttpSession session) {
        if (seckillSecretKey == null || !seckillSecretKey.equals(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_ORDER_SALT))) {
            throw new BusinessException("秒杀商品下单不合法");
        }
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String orderNo = orderService.seckillSaveOrder(seckillSuccessId, userVO);
        return R.success().add("orderNo", orderNo);
    }

    @ResponseBody
    @GetMapping("/saveOrder/result/{orderNo}")
    public R saveOrderResult(@PathVariable("orderNo") String orderNo) {
        String result = redisCache.getCacheObject(Constants.SAVE_ORDER_RESULT_KEY + orderNo);
        if (!Constants.SUCCESS.equals(result)) {
            throw new BusinessException(result == null ? Constants.ERROR : result);
        }
        return R.success();
    }

    @GetMapping("/orders/{orderNo}")
    public String orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderService.getOne(queryWrapper);
        if (order == null || !order.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("订单项异常");
        }
        return renderOrderDetail(request, order);
    }


    @GetMapping("/orders")
    public String orderListPage(HttpServletRequest request, HttpSession session) {
        Page<OrderListVO> page = getPage(request, Constants.ORDER_SEARCH_PAGE_LIMIT);
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = new Order();
        order.setUserId(mallUserVO.getUserId());
        IPage<OrderListVO> iPage = orderService.selectMyOrderPage(page, order);
        List<OrderListVO> orderListVOS = iPage.getRecords();
        for (OrderListVO orderListVO : orderListVOS) {
            orderListVO.setOrderStatusString(OrderStatusEnum.getOrderStatusEnumByStatus(orderListVO.getOrderStatus()).getName());
        }
        List<Long> longs = orderListVOS.stream().map(OrderListVO::getOrderId).collect(Collectors.toList());
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().in(CollectionUtils.isNotEmpty(longs), "order_id", longs));
        Map<Long, List<OrderItem>> longListMap = orderItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        for (OrderListVO orderListVO : orderListVOS) {
            if (longListMap.containsKey(orderListVO.getOrderId())) {
                List<OrderItem> orderItemList = longListMap.get(orderListVO.getOrderId());
                List<OrderItemVO> itemVOList = MyBeanUtil.copyList(orderItemList, OrderItemVO.class);
                orderListVO.setNewBeeMallOrderItemVOS(itemVOList);
            }
        }
        request.setAttribute("orderPageResult", iPage);
        request.setAttribute("path", "orders");
        return "mall/my-orders";
    }


    @PutMapping("/orders/{orderNo}/cancel")
    @ResponseBody
    public R cancelOrder(@PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.OREDER_PAID.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            throw new BusinessException("订单无法关闭");
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus());
        return R.result(orderService.updateById(order), "订单状态修改异常");
    }

    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public R finishOrder(@PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.OREDER_EXPRESS.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
        return R.result(orderService.updateById(order), "订单状态更新异常");
    }

    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", order.getTotalPrice());
        return "mall/pay-select";
    }

    @GetMapping("/payPage")
    public String payOrder(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession session, @RequestParam("payType") int payType) throws UnsupportedEncodingException {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", order.getTotalPrice());
        if (payType == 1) {
            request.setCharacterEncoding("utf-8");
            // 初始化
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                    alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSigntype());
            // 创建API对应的request
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            // 在公共参数中设置回调和通知地址
            String url = newbeeMallConfig.getServerUrl() + request.getContextPath();
            alipayRequest.setReturnUrl(url + "/returnOrders/" + order.getOrderNo() + "/" + mallUserVO.getUserId());
            alipayRequest.setNotifyUrl(url + "/paySuccess?payType=1&orderNo=" + order.getOrderNo());

            // 填充业务参数

            // 必填
            // 商户订单号，需保证在商户端不重复
            String out_trade_no = order.getOrderNo() + new Random().nextInt(9999);
            // 销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
            String product_code = "FAST_INSTANT_TRADE_PAY";
            // 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
            String total_amount = order.getTotalPrice() + "";
            // 订单标题
            String subject = "支付宝测试";

            // 选填
            // 商品描述，可空
            String body = "商品描述";

            alipayRequest.setBizContent("{" + "\"out_trade_no\":\"" + out_trade_no + "\"," + "\"product_code\":\""
                    + product_code + "\"," + "\"total_amount\":\"" + total_amount + "\"," + "\"subject\":\"" + subject
                    + "\"," + "\"body\":\"" + body + "\"}");
            // 请求
            String form;
            try {
                // 调用SDK生成表单
                form = alipayClient.pageExecute(alipayRequest).getBody();
                request.setAttribute("form", form);
            } catch (AlipayApiException e) {
                log.error(e.getMessage(), e);
            }
            return "mall/alipay";
        } else {
            return "mall/wxpay";
        }
    }

    @RequestMapping(value = "/paySuccess", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public R paySuccess(Byte payType, String orderNo) {
        log.info("支付宝paySuccess通知数据记录：orderNo: {}, payType：{}", orderNo, payType);
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new BusinessException("订单关闭异常");
        }
        order.setOrderStatus((byte) OrderStatusEnum.OREDER_PAID.getOrderStatus());
        order.setPayType(payType);
        order.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
        order.setPayTime(new Date());
        order.setUpdateTime(new Date());
        if (!orderService.updateById(order)) {
            throw new BusinessException("订单状态更新异常！");
        }
        // 支付成功清空订单支付超期任务
        taskService.removeTask(new OrderUnPaidTask(order.getOrderId()));
        return R.success();
    }

    @GetMapping("/returnOrders/{orderNo}/{userId}")
    public String returnOrderDetailPage(HttpServletRequest request, @PathVariable String orderNo, @PathVariable Long userId) {
        log.info("支付宝return通知数据记录：orderNo: {}, 当前登陆用户：{}", orderNo, userId);
        Order order = judgeOrderUserId(orderNo, userId);
        // 刷新页面，判断订单状态是否为已支付
        return renderOrderDetail(request, order);
    }


    /**
     * 判断订单关联用户id和当前登陆用户是否一致
     *
     * @param orderNo 订单编号
     * @param userId  用户ID
     * @return 返回订单对象
     */
    private Order judgeOrderUserId(String orderNo, Long userId) {
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        // 判断订单userId
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("当前订单用户异常");
        }
        return order;
    }

    /**
     * 渲染订单详情
     *
     * @param request 请求对象
     * @param order   订单详情
     * @return 模板路径
     */
    private String renderOrderDetail(HttpServletRequest request, Order order) {
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new BusinessException("订单项异常");
        }
        List<OrderItemVO> itemVOList = MyBeanUtil.copyList(orderItems, OrderItemVO.class);
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        BeanUtils.copyProperties(order, orderDetailVO);
        orderDetailVO.setOrderStatusString(OrderStatusEnum.getOrderStatusEnumByStatus(orderDetailVO.getOrderStatus()).getName());
        orderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(orderDetailVO.getPayType()).getName());
        orderDetailVO.setNewBeeMallOrderItemVOS(itemVOList);
        request.setAttribute("orderDetailVO", orderDetailVO);
        Coupon coupon = couponUserService.getCoupon(order.getOrderId());
        if (coupon != null) {
            request.setAttribute("discount", coupon.getDiscount());
        }
        return "mall/order-detail";
    }

}
