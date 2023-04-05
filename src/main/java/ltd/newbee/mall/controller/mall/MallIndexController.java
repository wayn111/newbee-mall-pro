package ltd.newbee.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.Carousels;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.service.CarouselsService;
import ltd.newbee.mall.core.service.GoodsCategoryService;
import ltd.newbee.mall.core.service.IndexConfigService;
import ltd.newbee.mall.enums.IndexConfigTypeEnum;
import ltd.newbee.mall.recommend.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static ltd.newbee.mall.constant.Constants.*;

@Controller
@AllArgsConstructor
public class MallIndexController {

    private GoodsCategoryService goodsCategoryService;
    private CarouselsService carouselsService;
    private IndexConfigService indexConfigService;
    private RecommendService recommendService;

    @GetMapping("index")
    public String index(HttpServletRequest request, HttpSession session) {
        LambdaQueryWrapper<Carousels> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Carousels::getIsDeleted, 0).orderByAsc(Carousels::getCarouselRank);
        // 分类数据
        request.setAttribute(CATEGORIES, goodsCategoryService.treeList());
        // 轮播图
        request.setAttribute(CAROUSELS, carouselsService.list(queryWrapper));
        // 热销商品
        request.setAttribute(HOT_GOODSES, indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_HOT, INDEX_GOODS_HOT_NUMBER));
        // 新品
        request.setAttribute(NEW_GOODSES, indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_NEW, INDEX_GOODS_NEW_NUMBER));
        // 推荐商品
        if (session.getAttribute(Constants.MALL_USER_SESSION_KEY) instanceof MallUserVO mallUserVO) {
            request.setAttribute(RECOMMEND_GOODSES, recommendService.recommendGoods(mallUserVO.getUserId(), INDEX_GOODS_RECOMMOND_NUMBER));
        } else {
            request.setAttribute(RECOMMEND_GOODSES, indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_RECOMMOND, INDEX_GOODS_RECOMMOND_NUMBER));
        }
        return "mall/index";
    }
}
