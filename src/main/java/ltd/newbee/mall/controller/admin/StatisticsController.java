package ltd.newbee.mall.controller.admin;

import ltd.newbee.mall.base.BaseController;
import ltd.newbee.mall.controller.vo.DayTransactionAmountVO;
import ltd.newbee.mall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("admin/statistics")
public class StatisticsController extends BaseController {

    private static final String PREFIX = "admin/statistics";

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String index(HttpServletRequest request) {
        List<DayTransactionAmountVO> countMallVOS = orderService.countMallTransactionAmount();
        ArrayList<String> xAxisData = new ArrayList<>();
        ArrayList<Long> seriesData = new ArrayList<>();
        for (DayTransactionAmountVO countMallVO : countMallVOS) {
            xAxisData.add(countMallVO.getDays());
            seriesData.add(countMallVO.getTotalPrice());
        }
        request.setAttribute("xAxisData", xAxisData);
        request.setAttribute("seriesData", seriesData);
        return PREFIX + "/statistics";
    }
}
