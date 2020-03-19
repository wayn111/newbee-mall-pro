package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.controller.vo.GoodsCategoryVO;
import ltd.newbee.mall.entity.GoodsCategory;

import java.util.List;

public interface GoodsCategoryService extends IService<GoodsCategory> {

    IPage selectPage(Page<GoodsCategory> page, GoodsCategory goodsCategory);

    List<GoodsCategoryVO> treeList();

}
