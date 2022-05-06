package ltd.newbee.mall;


import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.redis.JedisSearch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;

import java.io.IOException;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class JedisSearchTest {

    @Autowired
    private JedisSearch jedisSearch;

    @Autowired
    private GoodsService goodsService;

    private static final String idxName = "idx:goods";

    @Test
    public void createIndex() {
        System.out.println("begin");
        Schema schema = new Schema()
                .addSortableTextField("goodsName", 1.0)
                .addSortableTextField("goodsIntro", 0.5)
                .addSortableTagField("tag", "|");
        jedisSearch.createIndex(idxName, "goods", schema);
        System.out.println("end");
    }


    @Test
    public void query() {
        System.out.println("begin");
        SearchResult query = jedisSearch.query(idxName, "@goodsName:(手机) @goodsIntro:(手机)");
        System.out.println("end");
        assert query != null;
    }

    @Test
    public void addAll() throws IOException {
        System.out.println("begin");
        List<Goods> list = goodsService.list();
        jedisSearch.addGoodsListIndex(Constants.GOODS_IDX_PREFIX, list);
        System.out.println("end");
    }

    @Test
    public void goodsSync() {
        System.out.println("begin");
        jedisSearch.dropIndex(idxName);
        Schema schema = new Schema()
                .addSortableTextField("goodsName", 1.0)
                .addSortableTextField("goodsIntro", 0.5)
                .addSortableNumericField("sellingPrice")
                .addSortableNumericField("goodsId")
                .addSortableNumericField("originalPrice")
                .addSortableTagField("tag", "|");
        jedisSearch.createIndex(idxName, Constants.GOODS_IDX_PREFIX, schema);
        List<Goods> list = goodsService.list();
        jedisSearch.addGoodsListIndex(Constants.GOODS_IDX_PREFIX, list);
        SearchResult query = jedisSearch.query(idxName, "@tag:{小米|华为}");
        System.out.println("end");
    }
}
