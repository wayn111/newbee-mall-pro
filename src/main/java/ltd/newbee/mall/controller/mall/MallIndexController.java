package ltd.newbee.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.Carousels;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.vo.GoodsCategoryVO;
import ltd.newbee.mall.core.service.CarouselsService;
import ltd.newbee.mall.core.service.GoodsCategoryService;
import ltd.newbee.mall.core.service.IndexConfigService;
import ltd.newbee.mall.enums.IndexConfigTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MallIndexController {

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @Autowired
    private CarouselsService carouselsService;

    @Autowired
    private IndexConfigService indexConfigService;

    @GetMapping("index")
    public String index(HttpServletRequest request) {
        List<GoodsCategoryVO> root = goodsCategoryService.treeList();
        List<Goods> hotGoodses = indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_HOT, Constants.INDEX_GOODS_HOT_NUMBER);
        List<Goods> newGoodses = indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_NEW, Constants.INDEX_GOODS_NEW_NUMBER);
        List<Goods> recommendGoodses = indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_RECOMMOND, Constants.INDEX_GOODS_RECOMMOND_NUMBER);
        List<Carousels> carousels = carouselsService.list(new QueryWrapper<Carousels>()
                .eq("is_deleted", 0)
                .orderByAsc("carousel_rank"));
        // 分类数据
        request.setAttribute("categories", root);
        // 轮播图
        request.setAttribute("carousels", carousels);
        // 热销商品
        request.setAttribute("hotGoodses", hotGoodses);
        // 新品
        request.setAttribute("newGoodses", newGoodses);
        // 推荐商品
        request.setAttribute("recommendGoodses", recommendGoodses);
        return "mall/index";
    }
}
