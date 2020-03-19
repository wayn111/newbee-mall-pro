package ltd.newbee.mall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.base.BaseController;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.vo.*;
import ltd.newbee.mall.entity.Order;
import ltd.newbee.mall.entity.OrderItem;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.enums.PayStatusEnum;
import ltd.newbee.mall.enums.PayTypeEnum;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.service.OrderItemService;
import ltd.newbee.mall.service.OrderService;
import ltd.newbee.mall.service.ShopCatService;
import ltd.newbee.mall.util.MyBeanUtil;
import ltd.newbee.mall.util.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrderController extends BaseController {

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping("saveOrder")
    public String saveOrder(HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<ShopCatVO> shopcatVOList = shopCatService.getShopcatVOList(mallUserVO.getUserId());
        // 购物车中无数据则跳转至错误页
        if (CollectionUtils.isEmpty(shopcatVOList)) {
            throw new BusinessException("购物车中无数据");
        }
        String orderNo = orderService.saveOrder(mallUserVO, shopcatVOList);
        return redirectTo("orders/" + orderNo);
    }

    @GetMapping("/orders/{orderNo}")
    public String orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order != null && order.getUserId().equals(mallUserVO.getUserId())) {
            List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
            if (CollectionUtils.isNotEmpty(orderItems)) {
                List<OrderItemVO> itemVOList = MyBeanUtil.copyList(orderItems, OrderItemVO.class);
                OrderDetailVO orderDetailVO = new OrderDetailVO();
                BeanUtils.copyProperties(order, orderDetailVO);
                orderDetailVO.setOrderStatusString(OrderStatusEnum.getOrderStatusEnumByStatus(orderDetailVO.getOrderStatus()).getName());
                orderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(orderDetailVO.getPayType()).getName());
                orderDetailVO.setNewBeeMallOrderItemVOS(itemVOList);
                request.setAttribute("orderDetailVO", orderDetailVO);
                return "mall/order-detail";
            }
            throw new BusinessException("订单项异常");
        }
        throw new BusinessException("订单详情异常");
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
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        //todo 判断订单userId
        if (order == null || !order.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("当前订单用户异常");
        }
        //todo 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.OREDER_PAID.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            throw new BusinessException("订单关闭异常");
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus());
        orderService.updateById(order);
        return R.success();
    }

    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public R finishOrder(@PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        //todo 判断订单userId
        if (order == null || !order.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("当前订单用户异常");
        }
        //todo 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.OREDER_EXPRESS.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
        orderService.updateById(order);
        return R.success();
    }


    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        //todo 判断订单userId
        if (order == null || !order.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("当前订单用户异常");
        }
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
    public String payOrder(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession session, @RequestParam("payType") int payType) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        //todo 判断订单userId
        if (order == null || !order.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("当前订单用户异常");
        }
        //todo 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", order.getTotalPrice());
        if (payType == 1) {
            return "mall/alipay";
        } else {
            return "mall/wxpay";
        }
    }

    @GetMapping("/paySuccess")
    @ResponseBody
    public R paySuccess(@RequestParam("orderNo") String orderNo, @RequestParam("payType") int payType) {
        System.out.println(1324);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
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
