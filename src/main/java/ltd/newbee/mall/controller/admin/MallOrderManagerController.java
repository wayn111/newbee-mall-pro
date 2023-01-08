package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Coupon;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.OrderItem;
import ltd.newbee.mall.core.entity.vo.OrderItemVO;
import ltd.newbee.mall.core.entity.vo.OrderVO;
import ltd.newbee.mall.core.service.CouponUserService;
import ltd.newbee.mall.core.service.OrderItemService;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.enums.PayStatusEnum;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.util.MyBeanUtil;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("admin/orders")
public class MallOrderManagerController extends BaseController {

    private static final String PREFIX = "admin/order";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CouponUserService couponUserService;

    @GetMapping
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "orders");
        return PREFIX + "/order";
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage<Order> list(OrderVO orderVO, HttpServletRequest request) {
        Page<Order> page = getPage(request);
        return orderService.selectPage(page, orderVO);
    }

    @ResponseBody
    @GetMapping("order-items/{orderId}")
    public R getOrderItems(@PathVariable("orderId") Long orderId) {
        R success = R.success();
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        List<OrderItemVO> orderItemVOS = MyBeanUtil.copyList(orderItems, OrderItemVO.class);
        Coupon coupon = couponUserService.getCoupon(orderId);
        if (coupon != null) {
            success.add("discount", coupon.getDiscount());
        }
        return success.add("data", orderItemVOS);
    }

    @ResponseBody
    @PostMapping("update")
    public R update(@RequestBody Order order) {
        Order order1 = orderService.getById(order.getOrderId());
        if (order1 == null) {
            throw new BusinessException("未查询到该订单");
        }
        if (order1.getOrderStatus() > OrderStatusEnum.OREDER_PACKAGED.getOrderStatus()
                || order1.getOrderStatus() < OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            throw new BusinessException("当前订单无法更改");

        }
        return R.result(orderService.updateById(order));
    }


    /**
     * 配货
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("checkDone")
    public R checkDone(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() != OrderStatusEnum.OREDER_PAID.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未支付，不可配货");
            }
            updateOrderIds.add(order.getOrderId());
        }
        boolean update = orderService.update()
                .set("order_status", OrderStatusEnum.OREDER_PACKAGED.getOrderStatus())
                .in("order_id", updateOrderIds)
                .update();
        return R.result(update);
    }

    /**
     * 出库
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("checkOut")
    public R checkOut(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() != OrderStatusEnum.OREDER_PACKAGED.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未配货，不可出库");
            }
            updateOrderIds.add(order.getOrderId());
        }
        boolean update = orderService.update()
                .set("order_status", OrderStatusEnum.OREDER_EXPRESS.getOrderStatus())
                .in("order_id", updateOrderIds)
                .update();
        return R.result(update);
    }

    /**
     * 关闭订单
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("close")
    public R close(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() == OrderStatusEnum.ORDER_SUCCESS.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单已关闭");
            }
            if (order.getOrderStatus() != OrderStatusEnum.OREDER_EXPRESS.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未出库，不可关闭");
            }
            updateOrderIds.add(order.getOrderId());
        }
        boolean update = orderService.update()
                .set("order_status", OrderStatusEnum.ORDER_SUCCESS.getOrderStatus())
                .in("order_id", updateOrderIds)
                .update();
        return R.result(update);
    }

}
