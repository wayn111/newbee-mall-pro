package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import ltd.newbee.mall.core.dao.GoodsCategoryDao;
import ltd.newbee.mall.core.entity.GoodsCategory;
import ltd.newbee.mall.core.entity.vo.GoodsCategoryVO;
import ltd.newbee.mall.core.service.GoodsCategoryService;
import ltd.newbee.mall.util.MyBeanUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryDao, GoodsCategory> implements GoodsCategoryService {

    private GoodsCategoryDao goodsCategoryDao;

    @Override
    public IPage<GoodsCategory> selectPage(Page<GoodsCategory> page, GoodsCategory goodsCategory) {
        return goodsCategoryDao.selectListPage(page, goodsCategory);
    }

    @Override
    public List<GoodsCategoryVO> treeList() {
        // 查询所有分类
        List<GoodsCategory> list = list(new QueryWrapper<GoodsCategory>().eq("is_deleted", 0));
        List<GoodsCategoryVO> voList = MyBeanUtil.copyList(list, GoodsCategoryVO.class);
        List<GoodsCategoryVO> root = new ArrayList<>();
        for (GoodsCategoryVO goodsCategoryVO : voList) {
            // 添加一级分类
            if (goodsCategoryVO.getParentId() == 0) {
                root.add(goodsCategoryVO);
            }
            Long id = goodsCategoryVO.getCategoryId();
            List<GoodsCategoryVO> subList = new ArrayList<>();
            for (GoodsCategoryVO categoryVO : voList) {
                // 添加二级或者三级分类
                if (categoryVO.getParentId().equals(id)) {
                    subList.add(categoryVO);
                    goodsCategoryVO.setSubCategoryVOS(subList);
                }
            }
        }
        return root.subList(0, 9);
    }
}
