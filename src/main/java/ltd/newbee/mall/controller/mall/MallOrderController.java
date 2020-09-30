package ltd.newbee.mall.controller.mall;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.base.BaseController;
import ltd.newbee.mall.config.AlipayConfig;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.vo.*;
import ltd.newbee.mall.entity.Coupon;
import ltd.newbee.mall.entity.Order;
import ltd.newbee.mall.entity.OrderItem;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.enums.PayStatusEnum;
import ltd.newbee.mall.enums.PayTypeEnum;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.service.*;
import ltd.newbee.mall.util.MyBeanUtil;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.http.HttpUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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
    private CouponService couponService;

    @Autowired
    private AlipayConfig alipayConfig;

    @GetMapping("saveOrder")
    public String saveOrder(Long couponUserId, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<ShopCatVO> shopcatVOList = shopCatService.getShopCatVOList(mallUserVO.getUserId());
        // 购物车中无数据则跳转至错误页
        if (CollectionUtils.isEmpty(shopcatVOList)) {
            throw new BusinessException("购物车中无数据");
        }
        String orderNo = orderService.saveOrder(mallUserVO, couponUserId, shopcatVOList);
        return redirectTo("orders/" + orderNo);
    }

    @GetMapping("/orders/{orderNo}")
    public String orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null || !order.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("订单项异常");
        }
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
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().in("order_id", longs));
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
        Order order = judgeOrderUserId(orderNo, session);
        //todo 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.OREDER_PAID.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            throw new BusinessException("订单无法关闭");
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus());
        orderService.updateById(order);
        return R.success();
    }

    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public R finishOrder(@PathVariable("orderNo") String orderNo, HttpSession session) {
        Order order = judgeOrderUserId(orderNo, session);
        //todo 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.OREDER_EXPRESS.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
        orderService.updateById(order);
        return R.success();
    }

    /**
     * 判断订单关联用户id和当前登陆用户是否一致
     *
     * @param orderNo
     * @param session
     * @return
     */
    private Order judgeOrderUserId(String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        //todo 判断订单userId
        if (order == null || !order.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("当前订单用户异常");
        }
        return order;
    }


    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession session) {
        Order order = judgeOrderUserId(orderNo, session);
        //todo 判断订单状态
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
        Order order = judgeOrderUserId(orderNo, session);
        //todo 判断订单状态
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
            // 在公共参数中设置回跳和通知地址
            String url = HttpUtil.getRequestContext(request);
            alipayRequest.setReturnUrl(url + "/orders/" + order.getOrderNo());
            alipayRequest.setNotifyUrl(url + "/alipaySuccess/" + order.getOrderNo() + "/");

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
            String form = "";
            try {
                form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
                request.setAttribute("form", form);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            return "mall/alipay";
        } else {
            return "mall/wxpay";
        }
    }

    @RequestMapping("/alipaySuccess/{orderNo}/{payType}")
    public void alipaySuccess(@PathVariable("orderNo") String orderNo, @PathVariable("payType") int payType, HttpSession session) {
        Order order = judgeOrderUserId(orderNo, session);
        if (order != null) {
            //todo 判断订单状态
            if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                    || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
                throw new BusinessException("订单关闭异常");
            }
            order.setOrderStatus((byte) OrderStatusEnum.OREDER_PAID.getOrderStatus());
            order.setPayType((byte) payType);
            order.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            order.setPayTime(new Date());
            order.setUpdateTime(new Date());
            orderService.updateById(order);
        }
    }

    @GetMapping("/paySuccess")
    @ResponseBody
    public R paySuccess(@RequestParam("orderNo") String orderNo, @RequestParam("payType") int payType, HttpSession session) {
        Order order = judgeOrderUserId(orderNo, session);
        if (order != null) {
            //todo 判断订单状态
            if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                    || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
                throw new BusinessException("订单关闭异常");
            }
            order.setOrderStatus((byte) OrderStatusEnum.OREDER_PAID.getOrderStatus());
            order.setPayType((byte) payType);
            order.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            order.setPayTime(new Date());
            order.setUpdateTime(new Date());
            orderService.updateById(order);
        }
        return R.success();
    }


}
