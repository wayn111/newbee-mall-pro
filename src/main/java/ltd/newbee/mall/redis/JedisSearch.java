package ltd.newbee.mall.redis;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.dto.RsGoodsDTO;
import ltd.newbee.mall.core.entity.vo.SearchObjVO;
import ltd.newbee.mall.core.entity.vo.SearchPageGoodsVO;
import ltd.newbee.mall.util.ChineseUtil;
import ltd.newbee.mall.util.MyBeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.AbstractPipeline;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.search.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class JedisSearch {

    @Autowired
    private UnifiedJedis client;

    /**
     * 查询索引列表
     */
    public Set<String> listIndex() {
        return client.ftList();
    }

    /**
     * 删除索引
     *
     * @param idxName 索引名称
     */
    public void dropIndex(String idxName) {
        try {
            Set<String> strings = listIndex();
            if (strings.contains(idxName)) {
                client.ftDropIndex(idxName);
            }
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
        Long goodsCategoryId = searchObjVO.getGoodsCategoryId();
        // 查询商品名、商品介绍包含搜索词，同时是上架状态的商品
        String queryKey = "@goodsSellStatus:[0 0]";
        // 根据关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            if (ChineseUtil.hasChinese(keyword)) {
                queryKey += String.format(" ( @goodsName:(%s) | (@goodsIntro:(%s) | @tag:{%s} ))",
                        keyword, keyword, keyword);
            } else {
                queryKey += String.format(" ( @goodsNamePinyin:(%s) | (@goodsIntro:(%s) | @tag:{%s} ))",
                        "*" + keyword + "*", keyword, keyword);
            }
        }
        // 根据分类ID搜索
        if (goodsCategoryId != null) {
            queryKey += String.format(" @goodsCategoryId:[%s %s]", goodsCategoryId, goodsCategoryId);
        }
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
        List<List<Goods>> partition = ListUtil.partition(list, chunk);
        AbstractPipeline pipelined = client.pipelined();
        for (List<Goods> goodsList : partition) {
            for (Goods goods : goodsList) {
                RsGoodsDTO target = new RsGoodsDTO();
                MyBeanUtil.copyProperties(goods, target);
                Map<String, String> hash = MyBeanUtil.toMap(target);
                String pinyin = PinyinUtil.getPinyin(goods.getGoodsName(), "");
                hash.put("goodsNamePinyin", pinyin);
                // 支持中文
                hash.put("_language", Constants.GOODS_IDX_LANGUAGE);
                pipelined.hset(keyPrefix + goods.getGoodsId(), hash);
            }
        }
        pipelined.sync();
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
        List<String> keys = new ArrayList<>();
        // 游标初始值为0
        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams scanParams = new ScanParams();
        // 匹配 goodsIdxPrefix 为前缀的 key
        scanParams.match(goodsIdxPrefix + "*");
        scanParams.count(1000);
        while (true) {
            ScanResult<String> scanResult = client.scan(cursor, scanParams);
            cursor = scanResult.getCursor();
            // 返回0 说明遍历完成
            keys.addAll(scanResult.getResult());
            if (scanResult.isCompleteIteration()) {
                break;
            }
        }

        AbstractPipeline pipelined = client.pipelined();
        for (String key : keys) {
            pipelined.del(key);
        }
        pipelined.sync();
        return true;
    }

    /**
     * 同义词列表
     *
     * @param idxName
     * @return
     */
    public Map<String, List<String>> synonymsList(String idxName) {
        return client.ftSynDump(idxName);
    }

    /**
     * 同义词更新
     *
     * @param group
     * @param keywords
     * @return
     */
    public boolean synonymsUpdate(String idxName, String group, List<String> keywords) {
        return "OK".equals(client.ftSynUpdate(idxName, group, keywords.toArray(new String[]{})));
    }

}
