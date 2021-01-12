package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.IndexConfig;
import ltd.newbee.mall.enums.IndexConfigTypeEnum;
import ltd.newbee.mall.core.service.IndexConfigService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("admin/indexConfigs")
public class IndexCfgManagerController extends BaseController {

    private static final String PREFIX = "admin/indexConfigs";

    @Autowired
    private IndexConfigService indexConfigService;

    @GetMapping
    public String index(HttpServletRequest request, @RequestParam("configType") int configType) {
        IndexConfigTypeEnum indexConfigTypeEnumByType = IndexConfigTypeEnum.getIndexConfigTypeEnumByType(configType);
        request.setAttribute("path", indexConfigTypeEnumByType.getName());
        request.setAttribute("configType", configType);
        return PREFIX + "/index-configs";
    }

    @ResponseBody
    @GetMapping("/list")
    public IPage list(IndexConfig indexConfig, HttpServletRequest request) {
        Page<IndexConfig> page = getPage(request);
        return indexConfigService.selectPage(page, indexConfig);
    }

    /**
     * 保存
     *
     * @param indexConfig
     * @return
     */
    @ResponseBody
    @PostMapping("/save")
    public R save(@RequestBody IndexConfig indexConfig) {
        indexConfigService.save(indexConfig);
        return R.success();
    }


    /**
     * 更新
     *
     * @param indexConfig
     * @return
     */
    @ResponseBody
    @PostMapping("/update")
    public R update(@RequestBody IndexConfig indexConfig) {
        indexConfigService.updateById(indexConfig);
        return R.success();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ResponseBody
    public R delete(@RequestBody List<Integer> ids) {
        indexConfigService.removeByIds(ids);
        return R.success();
    }
}
