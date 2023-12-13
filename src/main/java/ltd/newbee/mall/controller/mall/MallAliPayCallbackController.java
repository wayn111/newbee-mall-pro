package ltd.newbee.mall.controller.mall;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.config.AlipayConfig;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.enums.PayStatusEnum;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.task.OrderUnPaidTask;
import ltd.newbee.mall.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("pay/callback")
public class MallAliPayCallbackController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AlipayConfig alipayConfig;

    @RequestMapping(value = "/aliPaySuccess", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String aliPaySuccess(HttpServletRequest request) {
        log.info("=======================支付宝回调==================request: {}", JSON.toJSONString(request.getParameterMap()));
        try {
            if ("RSA2".equals(request.getParameter("sign_type"))
                    && "TRADE_SUCCESS".equals(request.getParameter("trade_status"))
                    && verifySign(request)) {
                String orderNo = request.getParameter("out_trade_no");
                Order order = orderService.getOrderByOrderNo(orderNo);
                // 判断订单状态
                if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                        || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
                    throw new BusinessException("订单关闭异常");
                }
                order.setOrderStatus((byte) OrderStatusEnum.OREDER_PAID.getOrderStatus());
                order.setPayType((byte) 1);
                order.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
                order.setPayTime(new Date());
                order.setUpdateTime(new Date());
                if (!orderService.updateById(order)) {
                    throw new BusinessException("订单状态更新异常！");
                }
                // 支付成功清空订单支付超期任务
                taskService.removeTask(new OrderUnPaidTask(order.getOrderId()));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "error";
        }
        return "success";
    }


    /**
     * 验签
     */
    private boolean verifySign(HttpServletRequest request) throws AlipayApiException {
        // 编码
        String charset = request.getParameter("charset");
        // 签名算法类型
        String signType = request.getParameter("sign_type");
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            params.put(entry.getKey(), ArrayUtil.join(entry.getValue(), StrUtil.COMMA));
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), charset, signType);
        log.info("支付宝回调:verifySign params={},signVerified={}", JSON.toJSONString(params), signVerified);
        return signVerified;
    }
}
