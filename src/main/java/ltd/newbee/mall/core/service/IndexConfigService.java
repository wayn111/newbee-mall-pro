package ltd.newbee.mall.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.IndexConfig;
import ltd.newbee.mall.enums.IndexConfigTypeEnum;

import java.util.List;

public interface IndexConfigService extends IService<IndexConfig> {

    /**
     * 分页查询
     * @param page 分页对象
     * @param indexConfig 首页配置对象
     * @return 分页数据
     */
    IPage<IndexConfig> selectPage(Page<IndexConfig> page, IndexConfig indexConfig);

    /**
     * 查询首页热销商品，新品上线，推荐商品
     * @param indexConfigTypeEnum 首页配置枚举对象
     * @param limit 查询限制数量
     * @return 商品集合
     */
    List<Goods> listIndexConfig(IndexConfigTypeEnum indexConfigTypeEnum, int limit);
}
