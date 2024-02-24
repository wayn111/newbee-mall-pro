package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import ltd.newbee.mall.core.dao.IndexConfigDao;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.IndexConfig;
import ltd.newbee.mall.core.entity.vo.GoodsVO;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.core.service.IndexConfigService;
import ltd.newbee.mall.enums.IndexConfigTypeEnum;
import ltd.newbee.mall.util.MyBeanUtil;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IndexConfigServiceImpl extends ServiceImpl<IndexConfigDao, IndexConfig> implements IndexConfigService {

    @Resource
    private IndexConfigDao indexConfigDao;

    @Resource
    private GoodsService goodsService;

    @Override
    public IPage<IndexConfig> selectPage(Page<IndexConfig> page, IndexConfig indexConfig) {
        return indexConfigDao.selectListPage(page, indexConfig);
    }

    @Override
    public List<GoodsVO> listIndexConfig(IndexConfigTypeEnum indexGoodsHot, int limit) {
        List<IndexConfig> list = indexConfigDao.selectListIndexConfig(indexGoodsHot.getType(), limit);
        List<Long> collect = list.stream().map(IndexConfig::getGoodsId).collect(Collectors.toList());
        Map<Long, Integer> goodsIdMap = list.stream().collect(Collectors.toMap(IndexConfig::getGoodsId, IndexConfig::getConfigRank, (o1, o2) -> o1));
        List<Goods> goods = goodsService.listByIds(collect);
        List<GoodsVO> goodsVOS = MyBeanUtil.copyList(goods, GoodsVO.class);
        for (GoodsVO goodsVO : goodsVOS) {
            goodsVO.setConfigRank(goodsIdMap.getOrDefault(goodsVO.getGoodsId(), 999));
        }
        goodsVOS.sort(Comparator.comparingInt(GoodsVO::getConfigRank).reversed());
        return goodsVOS;
    }

}
