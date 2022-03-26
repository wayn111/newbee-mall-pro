package ltd.newbee.mall;


import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.service.SeckillService;
import ltd.newbee.mall.redis.JedisSearch;
import org.apache.ibatis.jdbc.Null;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@RunWith(SpringRunner.class)
public class JedisSearchTest {

    @Autowired
    private JedisSearch jedisSearch;

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
        SearchResult query = jedisSearch.query(idxName, "@tag:{小米|华为}");
        System.out.println("end");
        assert query != null;
    }

    @Test
    public void addAll() throws IOException {
        System.out.println("begin");
        jedisSearch.addAll("goods:");
        System.out.println("end");
    }

    @Test
    public void goodsSync() throws IOException {
        System.out.println("begin");
        jedisSearch.dropIndex(idxName);
        Schema schema = new Schema()
                .addSortableTextField("goodsName", 1.0)
                .addSortableTextField("goodsIntro", 0.5)
                .addSortableNumericField("sellingPrice")
                .addSortableNumericField("originalPrice")
                .addSortableTagField("tag", "|");
        jedisSearch.createIndex(idxName, "goods:", schema);
        jedisSearch.addAll("goods:");
        SearchResult query = jedisSearch.query(idxName, "@tag:{小米|华为}");
        System.out.println("end");
    }
}
