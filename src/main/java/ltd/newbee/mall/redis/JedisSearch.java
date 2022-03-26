package ltd.newbee.mall.redis;

import com.alibaba.fastjson.JSONObject;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.util.MyBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JedisSearch {

    @Autowired
    private UnifiedJedis client;

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private GoodsService goodsService;

    public void dropIndex(String idxName) {
        try {
            client.ftDropIndex(idxName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createIndex(String idxName, String prefix, Schema schema) {
        IndexDefinition rule = new IndexDefinition(IndexDefinition.Type.HASH)
                .setPrefixes(prefix)
                .setLanguage("chinese");
        client.ftCreate(idxName,
                IndexOptions.defaultOptions().setDefinition(rule),
                schema);
    }

    public SearchResult query(String idxName, String search) {
        Query q = new Query(search);
        q.setLanguage("chinese");
        return client.ftSearch(idxName, q);
    }


    public void addAll(String keyPrefix) throws IOException {
        List<Goods> list = goodsService.list();
        for (Goods goods : list) {
            Map<String, String> hash = MyBeanUtil.toMap(goods);
            hash.put("_language", "chinese");
            client.hset(keyPrefix + goods.getGoodsId(), MyBeanUtil.toMap(goods));
        }

    }
}
