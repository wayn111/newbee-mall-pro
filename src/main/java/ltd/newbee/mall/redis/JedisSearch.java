package ltd.newbee.mall.redis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.dto.RsGoodsDTO;
import ltd.newbee.mall.core.entity.vo.SearchObjVO;
import ltd.newbee.mall.core.entity.vo.SearchPageGoodsVO;
import ltd.newbee.mall.util.MyBeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class JedisSearch {

    @Autowired
    private UnifiedJedis client;

    /**
     * 删除索引
     *
     * @param idxName 索引名称
     */
    public void dropIndex(String idxName) {
        try {
            client.ftDropIndex(idxName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 创建索引
     *
     * @param idxName 索引名称
     * @param prefix  要索引的数据前缀
     * @param schema  索引字段配置
     */
    public void createIndex(String idxName, String prefix, Schema schema) {
        IndexDefinition rule = new IndexDefinition(IndexDefinition.Type.HASH)
                .setPrefixes(prefix)
                .setLanguage(Constants.GOODS_IDX_LANGUAGE);
        client.ftCreate(idxName,
                IndexOptions.defaultOptions().setDefinition(rule),
                schema);
    }

    /**
     * 查询
     *
     * @param idxName 索引名称
     * @param search  查询key
     * @param sort    排序字段
     * @return searchResult
     */
    public SearchResult query(String idxName, String search, String sort) {
        Query q = new Query(search);
        if (StringUtils.isNotBlank(sort)) {
            q.setSortBy(sort, false);
        }
        q.setLanguage(Constants.GOODS_IDX_LANGUAGE);
        q.limit(0, 10);
        return client.ftSearch(idxName, q);
    }

    public SearchResult queryAll(String idxName, String search, String sort) {
        Query q = new Query(search);
        if (StringUtils.isNotBlank(sort)) {
            q.setSortBy(sort, false);
        }
        q.setLanguage(Constants.GOODS_IDX_LANGUAGE);
        q.limit(0, 10000);
        return client.ftSearch(idxName, q);
    }

    public SearchResult search(String goodsIdxName, SearchObjVO searchObjVO, Page<SearchPageGoodsVO> page) {
        String keyword = searchObjVO.getKeyword();
        // 查询商品名、商品介绍包含搜索词，同时是上架状态的商品
        String queryKey = String.format("(@goodsName:(%s)) | (@goodsIntro:(%s) | @tag:{%s}) @goodsSellStatus:[0 0]", keyword, keyword, keyword);
        Query q = new Query(queryKey);
        String sort = searchObjVO.getSidx();
        String order = searchObjVO.getOrder();
        if (StringUtils.isNotBlank(sort)) {
            q.setSortBy(sort, Constants.SORT_ASC.equals(order));
        }
        q.setLanguage(Constants.GOODS_IDX_LANGUAGE);
        q.limit((int) page.offset(), (int) page.getSize());
        return client.ftSearch(goodsIdxName, q);
    }

    /**
     * 添加索引数据
     *
     * @param keyPrefix 要索引的数据前缀
     * @param goods     商品信息
     * @return boolean
     */
    public boolean addGoodsIndex(String keyPrefix, Goods goods) {
        goods2RsGoods(keyPrefix, goods);
        return true;
    }

    /**
     * 同步商品索引
     *
     * @param keyPrefix 要索引的数据前缀
     * @return boolean
     */
    public boolean addGoodsListIndex(String keyPrefix, List<Goods> list) {
        int chunk = 200;
        int size = list.size();
        int ceil = (int) Math.ceil(size / (double) chunk);
        List<CompletableFuture<Void>> futures = new ArrayList<>(4);
        for (int i = 0; i < ceil; i++) {
            int toIndex = (i + 1) * chunk;
            if (toIndex > size) {
                toIndex = i * chunk + size % chunk;
            }
            List<Goods> subList = list.subList(i * chunk, toIndex);
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.supplyAsync(() -> subList).thenAccept(goodsList -> {
                for (Goods goods : goodsList) {
                    goods2RsGoods(keyPrefix, goods);
                }
            });
            futures.add(voidCompletableFuture);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return true;
    }

    private void goods2RsGoods(String keyPrefix, Goods goods) {
        RsGoodsDTO target = new RsGoodsDTO();
        MyBeanUtil.copyProperties(goods, target);
        Map<String, String> hash = MyBeanUtil.toMap(target);
        // 支持中文
        hash.put("_language", Constants.GOODS_IDX_LANGUAGE);
        client.hset(keyPrefix + goods.getGoodsId(), hash);
    }

    public boolean deleteGoodsList(String goodsIdxPrefix) {
        Set<String> keys = client.keys(goodsIdxPrefix + "*");
        for (String key : keys) {
            client.del(key);
        }
        return true;
    }
}
