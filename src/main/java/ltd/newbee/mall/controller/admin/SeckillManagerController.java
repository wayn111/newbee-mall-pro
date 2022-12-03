package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Seckill;
import ltd.newbee.mall.core.entity.vo.SeckillVO;
import ltd.newbee.mall.core.service.SeckillService;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("admin/seckill")
public class SeckillManagerController extends BaseController {

    private static final String PREFIX = "admin/seckill";

    @Autowired
    private SeckillService seckillService;
    @Autowired
    private RedisCache redisCache;

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
    public IPage<Seckill> list(SeckillVO seckillVO, HttpServletRequest request) {
        Page<Seckill> page = getPage(request);
        return seckillService.selectPage(page, seckillVO);
    }

    @ResponseBody
    @PostMapping("/save")
    public R save(@RequestBody Seckill seckill) {
        boolean save = seckillService.save(seckill);
        if (save) {
            // 库存预热
            redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckill.getSeckillId(), seckill.getSeckillNum());
            redisCache.deleteObject(Constants.SECKILL_GOODS_LIST_HTML);
        }
        return R.result(save);
    }

    @ResponseBody
    @PostMapping("/update")
    public R update(@RequestBody Seckill seckill) {
        seckill.setUpdateTime(new Date());
        boolean update = seckillService.updateById(seckill);
        if (update) {
            // 库存预热
            Long seckillId = seckill.getSeckillId();
            redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckillId, seckill.getSeckillNum());
            redisCache.deleteObject(Constants.SECKILL_GOODS_LIST_HTML);
            redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL_HTML + seckillId);
        }
        return R.result(update);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public R Info(@PathVariable("id") Long id) {
        return R.success().add("data", seckillService.getById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public R delete(@PathVariable("id") Long id) {
        return R.result(seckillService.removeById(id));
    }
}
