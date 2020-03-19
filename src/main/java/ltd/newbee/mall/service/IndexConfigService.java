package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.Goods;
import ltd.newbee.mall.entity.IndexConfig;
import ltd.newbee.mall.enums.IndexConfigTypeEnum;

import java.util.List;

public interface IndexConfigService extends IService<IndexConfig> {
    IPage selectPage(Page<IndexConfig> page, IndexConfig indexConfig);

    /**
     * 查询首页热销商品，新品上线，推荐商品
     * @param indexGoodsHot
     * @param limit
     * @return
     */
    List<Goods> listIndexConfig(IndexConfigTypeEnum indexGoodsHot, int limit);
}
