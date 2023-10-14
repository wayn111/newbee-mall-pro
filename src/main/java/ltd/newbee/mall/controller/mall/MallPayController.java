package ltd.newbee.mall.controller.mall;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.config.AlipayConfig;
import ltd.newbee.mall.config.NewbeeMallConfig;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.enums.OrderStatusEnum;
import ltd.newbee.mall.enums.PayStatusEnum;
import ltd.newbee.mall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.util.Random;

@Slf4j
@Controller
public class MallPayController extends BaseController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private NewbeeMallConfig newbeeMallConfig;

    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.judgeOrderUserId(orderNo, mallUserVO.getUserId());
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
        Order order = orderService.judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", order.getTotalPrice());
        if (payType == 1) {
            request.setCharacterEncoding("utf-8");
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                    alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSigntype());
            // 商户订单号，需保证在商户端不重复
            String out_trade_no = order.getOrderNo();
            // 销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
            String product_code = "FAST_INSTANT_TRADE_PAY";
            // 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
            String total_amount = order.getTotalPrice() + "";
            // 订单标题
            String subject = "商城订单";

            AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
            // 异步接收地址，仅支持http/https，公网可访问
            alipayTradePagePayRequest.setNotifyUrl(newbeeMallConfig.getServerUrl() + "/pay/callback/aliPaySuccess");
            alipayTradePagePayRequest.setReturnUrl(newbeeMallConfig.getServerUrl() + "/returnOrders/" + order.getOrderNo() + "/" + mallUserVO.getUserId());

            /******必传参数******/
            JSONObject bizContent = new JSONObject();
            // 商户订单号，商家自定义，保持唯一性
            bizContent.put("out_trade_no", out_trade_no);
            // 支付金额，最小值0.01元
            bizContent.put("total_amount", total_amount);
            // 订单标题，不可使用特殊符号
            bizContent.put("subject", subject);
            // 电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
            bizContent.put("product_code", product_code);

            alipayTradePagePayRequest.setBizContent(bizContent.toString());
            AlipayTradePagePayResponse response;
            try {
                log.info("alipayTradePagePayRequest:{}", JSON.toJSONString(alipayTradePagePayRequest));
                response = alipayClient.pageExecute(alipayTradePagePayRequest);
                if (response.isSuccess()) {
                    // 调用SDK生成表单
                    String form = response.getBody();
                    request.setAttribute("form", form);
                }
            } catch (AlipayApiException e) {
                log.error(e.getMessage(), e);
            }
            return "mall/alipay";
        } else {
            return "mall/wxpay";
        }
    }
}
