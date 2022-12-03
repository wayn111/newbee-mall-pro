package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Carousels;
import ltd.newbee.mall.core.service.CarouselsService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("admin/carousels")
public class CarouselsManagerController extends BaseController {

    private static final String PREFIX = "admin/carousels";

    @Autowired
    private CarouselsService carouselsService;

    @GetMapping
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "newbee_mall_carousel");
        return PREFIX + "/carousels";
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage<Carousels> list(Carousels carousels, HttpServletRequest request) {
        Page<Carousels> page = getPage(request);
        return carouselsService.selectPage(page, carousels);
    }

    /**
     * 列表
     */
    @PostMapping("/save")
    @ResponseBody
    public R save(@RequestBody Carousels carousels) {
        baseFieldHandle(carousels, true);
        return R.result(carouselsService.save(carousels));
    }

    /**
     * 列表
     */
    @PostMapping("/update")
    @ResponseBody
    public R update(@RequestBody Carousels carousels) {
        baseFieldHandle(carousels, false);
        return R.result(carouselsService.updateById(carousels));
    }

    /**
     * 详情
     */
    @GetMapping("/info/{id}")
    @ResponseBody
    public R Info(@PathVariable("id") Integer id) {
        return R.success().add("data", carouselsService.getById(id));
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ResponseBody
    public R delete(@RequestBody List<Integer> ids) {
        return R.result(carouselsService.removeByIds(ids));
    }
}
