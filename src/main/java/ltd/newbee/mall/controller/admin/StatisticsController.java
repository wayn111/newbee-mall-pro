package ltd.newbee.mall.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.vo.DayTransactionAmountVO;
import ltd.newbee.mall.core.service.OrderService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("admin/statistics")
public class StatisticsController extends BaseController {

    private static final String PREFIX = "admin/statistics";

    private static final Integer DEFAULT_DAY_NUM = 365;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String index(HttpServletRequest request) {
        List<DayTransactionAmountVO> countMallVOS = orderService.countMallTransactionAmount(DEFAULT_DAY_NUM);
        ArrayList<String> xAxisData = new ArrayList<>();
        ArrayList<Long> seriesData = new ArrayList<>();
        for (DayTransactionAmountVO countMallVO : countMallVOS) {
            xAxisData.add(countMallVO.getDays());
            seriesData.add(countMallVO.getTotalPrice());
        }
        request.setAttribute("defaultDayNum", DEFAULT_DAY_NUM);
        request.setAttribute("xAxisData", xAxisData);
        request.setAttribute("seriesData", seriesData);
        return PREFIX + "/statistics";
    }

    @ResponseBody
    @GetMapping("/transactionAmount/{dayNum}")
    public R selectDayTransactionAmount(@PathVariable("dayNum") Integer dayNum) {
        List<DayTransactionAmountVO> countMallVOS = orderService.countMallTransactionAmount(dayNum);
        ArrayList<String> xAxisData = new ArrayList<>();
        ArrayList<Long> seriesData = new ArrayList<>();
        for (DayTransactionAmountVO countMallVO : countMallVOS) {
            xAxisData.add(countMallVO.getDays());
            seriesData.add(countMallVO.getTotalPrice());
        }
        return R.success().add("xAxisData", xAxisData).add("seriesData", seriesData);
    }
}
