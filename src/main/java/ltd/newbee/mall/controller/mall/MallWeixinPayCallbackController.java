package ltd.newbee.mall.controller.mall;

import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.enums.PayStatusEnum;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.task.OrderUnPaidTask;
import ltd.newbee.mall.task.TaskService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Slf4j
@Controller
@RequestMapping("pay/callback")
public class MallWeixinPayCallbackController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TaskService taskService;

    /**
     * 测试微信支付直接付钱
     * @param payType
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "/weixinPaySuccess", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public R weixinPaySuccess(Byte payType, String orderNo) {
        log.info("微信paySuccess通知数据记录：orderNo: {}, payType：{}", orderNo, payType);
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new BusinessException("订单关闭异常");
        }
        order.setOrderStatus((byte) OrderStatusEnum.OREDER_PAID.getOrderStatus());
        order.setPayType((byte) 2);
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

}
