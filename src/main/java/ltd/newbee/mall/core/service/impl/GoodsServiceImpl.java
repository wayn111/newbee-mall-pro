package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.dao.GoodsDao;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.vo.SearchObjVO;
import ltd.newbee.mall.core.entity.vo.SearchPageGoodsVO;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.redis.JedisSearch;
import org.springframework.stereotype.Service;
import redis.clients.jedis.search.Schema;

import java.util.List;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsDao, Goods> implements GoodsService {

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private JedisSearch jedisSearch;

    @Override
    public IPage<Goods> selectPage(Page<Goods> page, Goods goods) {
        return goodsDao.selectListPage(page, goods);
    }

    @Override
    public IPage<Goods> findMallGoodsListBySearch(Page<SearchPageGoodsVO> page, SearchObjVO searchObjVO) {
        return goodsDao.findMallGoodsListBySearch(page, searchObjVO);
    }

    @Override
    public boolean syncRs() {
        jedisSearch.dropIndex(Constants.GOODS_IDX_NAME);
        Schema schema = new Schema()
                .addSortableTextField("goodsName", 1.0)
                .addSortableTextField("goodsIntro", 0.5)
                .addSortableNumericField("goodsId")
                .addSortableNumericField("goodsSellStatus")
                .addSortableNumericField("sellingPrice")
                .addSortableNumericField("originalPrice")
                .addSortableTagField("tag", "|");
        jedisSearch.createIndex(Constants.GOODS_IDX_NAME, Constants.GOODS_IDX_PREFIX, schema);
        List<Goods> list = this.list();
        jedisSearch.deleteGoodsList(Constants.GOODS_IDX_PREFIX);
        return jedisSearch.addGoodsListIndex(Constants.GOODS_IDX_PREFIX, list);
    }

    @Override
    public boolean saveGoods(Goods goods) {
        this.save(goods);
        return jedisSearch.addGoodsIndex(Constants.GOODS_IDX_PREFIX, goods);
    }

    @Override
    public boolean updateGoods(Goods goods) {
        this.updateById(goods);
        return jedisSearch.addGoodsIndex(Constants.GOODS_IDX_PREFIX, goods);
    }

    @Override
    public boolean reduceStock(Long goodsId, Integer goodsCount) {
        return goodsDao.reduceStock(goodsId, goodsCount);
    }

    @Override
    public boolean changeSellStatus(List<Long> ids, int sellStatus) {
        lambdaUpdate().set(Goods::getGoodsSellStatus, sellStatus).in(Goods::getGoodsId, ids).update();
        List<Goods> list = goodsDao.selectGoodsListByIds(ids);
        return jedisSearch.addGoodsListIndex(Constants.GOODS_IDX_PREFIX, list);
    }

}
