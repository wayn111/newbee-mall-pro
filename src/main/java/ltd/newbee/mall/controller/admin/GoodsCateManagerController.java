package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.GoodsCategory;
import ltd.newbee.mall.core.service.GoodsCategoryService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("admin/categories")
public class GoodsCateManagerController extends BaseController {

    private static final String PREFIX = "admin/category";

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @GetMapping
    public String index(GoodsCategory goodsCategory, String backParentId, HttpServletRequest request) {
        request.setAttribute("path", "newbee_mall_category");
        request.setAttribute("categoryLevel", goodsCategory.getCategoryLevel());
        request.setAttribute("parentId", goodsCategory.getParentId());
        request.setAttribute("backParentId", backParentId);
        return PREFIX + "/category";
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage<GoodsCategory> list(GoodsCategory goodsCategory, HttpServletRequest request) {
        Page<GoodsCategory> page = getPage(request);
        return goodsCategoryService.selectPage(page, goodsCategory);
    }

    /**
     * 列表
     */
    @GetMapping(value = "/listForSelect")
    @ResponseBody
    public R listForSelect(@RequestParam("categoryId") Long categoryId) {
        GoodsCategory category = goodsCategoryService.getById(categoryId);
        // 既不是一级分类也不是二级分类则为不返回数据
        if (category == null || category.getCategoryLevel().equals(Constants.CATEGORY_LEVEL_THREE)) {
            return R.error("参数异常！");
        }
        Map<String, List<GoodsCategory>> categoryResult = new HashMap<>(2);
        // 如果是一级分类则返回当前一级分类下的所有二级分类，以及二级分类列表中第一条数据下的所有三级分类列表
        if (category.getCategoryLevel().equals(Constants.CATEGORY_LEVEL_ONE)) {
            // 查询一级分类列表中第一个实体的所有二级分类
            List<GoodsCategory> secondLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                    .eq("category_level", Constants.CATEGORY_LEVEL_TWO).eq("parent_id", categoryId));
            if (!CollectionUtils.isEmpty(secondLevelCategories)) {
                // 查询二级分类列表中第一个实体的所有三级分类
                List<GoodsCategory> thirdLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                        .eq("category_level", Constants.CATEGORY_LEVEL_THREE).eq("parent_id", secondLevelCategories.get(0).getCategoryId()));
                categoryResult.put("secondLevelCategories", secondLevelCategories);
                categoryResult.put("thirdLevelCategories", thirdLevelCategories);
            }
        }
        // 如果是二级分类则返回当前分类下的所有三级分类列表
        if (category.getCategoryLevel().equals(Constants.CATEGORY_LEVEL_TWO)) {
            // 查询二级分类列表中第一个实体的所有三级分类
            List<GoodsCategory> thirdLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                    .eq("category_level", Constants.CATEGORY_LEVEL_THREE).eq("parent_id", categoryId));
            categoryResult.put("thirdLevelCategories", thirdLevelCategories);
        }
        return R.success().add("data", categoryResult);
    }

    /**
     * 列表
     */
    @PostMapping("/save")
    @ResponseBody
    public R save(@RequestBody GoodsCategory goodsCategory) {
        return R.result(goodsCategoryService.save(goodsCategory));
    }

    /**
     * 列表
     */
    @PostMapping("/update")
    @ResponseBody
    public R update(@RequestBody GoodsCategory goodsCategory) {
        return R.result(goodsCategoryService.updateById(goodsCategory));
    }

    /**
     * 详情
     */
    @GetMapping("/info/{id}")
    @ResponseBody
    public R Info(@PathVariable("id") Integer id) {
        return R.success().add("data", goodsCategoryService.getById(id));
    }

    /**
     * 删除当前分类以及所有子分类
     */
    @PostMapping("/delete")
    @ResponseBody
    public R delete(@RequestBody List<Long> ids) {
        List<Long> list = new ArrayList<>(ids);
        for (Long id : ids) {
            List<GoodsCategory> goodsCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>().select("category_id").eq("parent_id", id));
            if (CollectionUtils.isNotEmpty(goodsCategories)) {
                List<Long> collect = goodsCategories.stream().map(GoodsCategory::getCategoryId).toList();
                list.addAll(collect);
                for (Long aLong : collect) {
                    List<GoodsCategory> list2 = goodsCategoryService.list(new QueryWrapper<GoodsCategory>().select("category_id").eq("parent_id", aLong));
                    List<Long> collect1 = list2.stream().map(GoodsCategory::getCategoryId).toList();
                    list.addAll(collect1);
                }
            }
        }
        return R.result(goodsCategoryService.removeByIds(list));
    }
}
