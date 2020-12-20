package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.base.BaseController;
import ltd.newbee.mall.entity.Seckill;
import ltd.newbee.mall.entity.vo.SeckillVO;
import ltd.newbee.mall.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("admin/seckill")
public class SeckillManagerController extends BaseController {

    private static final String PREFIX = "admin/seckill";

    @Autowired
    private SeckillService seckillService;

    @GetMapping
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "seckill");
        return PREFIX + "/seckill";
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage list(SeckillVO seckillVO, HttpServletRequest request) {
        Page<Seckill> page = getPage(request);
        return seckillService.selectPage(page, seckillVO);
    }
}
