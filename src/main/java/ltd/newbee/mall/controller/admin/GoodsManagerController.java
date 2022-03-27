package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.GoodsCategory;
import ltd.newbee.mall.core.service.GoodsCategoryService;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("admin/goods")
public class GoodsManagerController extends BaseController {

    private static final String PREFIX = "admin/goods";

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @GetMapping
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "newbee_mall_goods");
        return PREFIX + "/goods";
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage<Goods> list(Goods goods, HttpServletRequest request) {
        Page<Goods> page = getPage(request);
        return goodsService.selectPage(page, goods);
    }


    @GetMapping("/add")
    public String add(HttpServletRequest request) {
        request.setAttribute("path", "goods-add-edit");
        // 查询所有的一级分类
        List<GoodsCategory> firstLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                .eq("category_level", Constants.CATEGORY_LEVEL_ONE).eq("parent_id", 0));
        if (CollectionUtils.isNotEmpty(firstLevelCategories)) {
            // 查询一级分类列表中第一个实体的所有二级分类
            List<GoodsCategory> secondLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                    .eq("category_level", Constants.CATEGORY_LEVEL_TWO).eq("parent_id", firstLevelCategories.get(0).getCategoryId()));
            if (CollectionUtils.isNotEmpty(secondLevelCategories)) {
                // 查询一级分类列表中第一个实体的所有二级分类
                List<GoodsCategory> thirdLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                        .eq("category_level", Constants.CATEGORY_LEVEL_THREE).eq("parent_id", secondLevelCategories.get(0).getCategoryId()));
                request.setAttribute("firstLevelCategories", firstLevelCategories);
                request.setAttribute("secondLevelCategories", secondLevelCategories);
                request.setAttribute("thirdLevelCategories", thirdLevelCategories);
            }
        }
        return PREFIX + "/add-edit";
    }

    @GetMapping("/edit/{goodsId}")
    public String edit(@PathVariable("goodsId") Long goodsId, HttpServletRequest request) {
        request.setAttribute("path", "goods-add-edit");
        Goods goods = goodsService.getById(goodsId);
        request.setAttribute("goods", goods);

        if (goods.getGoodsCategoryId() == 0) {
            // 查询所有的一级分类
            List<GoodsCategory> firstLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                    .eq("category_level", Constants.CATEGORY_LEVEL_ONE).eq("parent_id", 0));
            if (CollectionUtils.isNotEmpty(firstLevelCategories)) {
                // 查询一级分类列表中第一个实体的所有二级分类
                List<GoodsCategory> secondLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                        .eq("category_level", Constants.CATEGORY_LEVEL_TWO).eq("parent_id", firstLevelCategories.get(0).getCategoryId()));
                if (CollectionUtils.isNotEmpty(secondLevelCategories)) {
                    // 查询一级分类列表中第一个实体的所有二级分类
                    List<GoodsCategory> thirdLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                            .eq("category_level", Constants.CATEGORY_LEVEL_THREE).eq("parent_id", secondLevelCategories.get(0).getCategoryId()));
                    request.setAttribute("firstLevelCategories", firstLevelCategories);
                    request.setAttribute("secondLevelCategories", secondLevelCategories);
                    request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                }
            }
        } else {
            // 根据商品分类id查询三级分类以及三级分类集合
            GoodsCategory thirdGoodsCategory = goodsCategoryService.getById(goods.getGoodsCategoryId());
            if (thirdGoodsCategory != null && thirdGoodsCategory.getCategoryLevel().equals(Constants.CATEGORY_LEVEL_THREE)) {
                List<GoodsCategory> thirdLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                        .eq("category_level", Constants.CATEGORY_LEVEL_THREE).eq("parent_id", thirdGoodsCategory.getParentId()));
                // 根据三级分类parentId查询二级分类以及二级分类集合
                GoodsCategory secondGoodsCategory = goodsCategoryService.getById(thirdGoodsCategory.getParentId());
                if (secondGoodsCategory != null && secondGoodsCategory.getCategoryLevel().equals(Constants.CATEGORY_LEVEL_TWO)) {
                    List<GoodsCategory> secondLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                            .eq("category_level", Constants.CATEGORY_LEVEL_TWO).eq("parent_id", secondGoodsCategory.getParentId()));
                    // 根据二级分类parentId查询一级分类以及一级分类集合
                    GoodsCategory firstGoodsCategory = goodsCategoryService.getById(secondGoodsCategory.getParentId());
                    if (firstGoodsCategory != null && firstGoodsCategory.getCategoryLevel().equals(Constants.CATEGORY_LEVEL_ONE)) {
                        List<GoodsCategory> firstLevelCategories = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                                .eq("category_level", Constants.CATEGORY_LEVEL_ONE).eq("parent_id", firstGoodsCategory.getParentId()));
                        // 所有分类数据都得到之后放到request对象中供前端读取
                        request.setAttribute("firstLevelCategories", firstLevelCategories);
                        request.setAttribute("secondLevelCategories", secondLevelCategories);
                        request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                        request.setAttribute("firstLevelCategoryId", firstGoodsCategory.getCategoryId());
                        request.setAttribute("secondLevelCategoryId", secondGoodsCategory.getCategoryId());
                        request.setAttribute("thirdLevelCategoryId", thirdGoodsCategory.getCategoryId());
                    }
                }
            }
        }
        return PREFIX + "/add-edit";
    }

    /**
     * 批量修改销售状态
     */
    @PutMapping(value = "/status/{sellStatus}")
    @ResponseBody
    public R delete(@RequestBody List<Long> ids, @PathVariable("sellStatus") int sellStatus) {
        return R.result(goodsService.update().set("goods_sell_status", sellStatus).in("goods_id", ids).update());
    }

    /**
     * 列表
     */
    @PostMapping("/save")
    @ResponseBody
    public R save(@RequestBody Goods goods) {
        return R.result(goodsService.save(goods));
    }

    /**
     * 列表
     */
    @PostMapping("/update")
    @ResponseBody
    public R update(@RequestBody Goods goods) {
        return R.result(goodsService.updateById(goods));
    }


    /**
     * 同步redisSearch
     */
    @PostMapping("/syncRs")
    @ResponseBody
    public R syncRs() {
        return R.result(goodsService.syncRs());
    }

}
