package ltd.newbee.mall;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.redis.JedisSearch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;

import java.util.List;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class JedisSearchTest {

    @Autowired
    private JedisSearch jedisSearch;

    @Autowired
    private GoodsService goodsService;

    private static final String IDX_NAME = "idx:goods";

    @Test
    public void createIndex() {
        log.info("begin");
        Schema schema = new Schema().addSortableTextField("goodsName", 1.0).addSortableTextField("goodsIntro", 0.5).addSortableTagField("tag", "|");
        jedisSearch.createIndex(IDX_NAME, "goods", schema);
        log.info("end");
    }


    @Test
    public void query() {
        log.info("begin");
        SearchResult query = jedisSearch.query(IDX_NAME, "(@goodsName:(手机) | @goodsIntro:(手机)) @goodsSellStatus:[0 0]", "sellingPrice");
        log.info("end");
        assert query != null;
    }

    @Test
    public void queryAll() {
        log.info("begin");
        SearchResult query = jedisSearch.queryAll(IDX_NAME, "*", null);
        log.info(String.valueOf(query.getDocuments().size()));
        log.info("end");
        assert query != null;
    }

    @Test
    public void addAll() {
        System.out.println("begin");
        List<Goods> list = goodsService.list();
        jedisSearch.addGoodsListIndex(Constants.GOODS_IDX_PREFIX, list);
        System.out.println("end");
    }

    @Test
    public void goodsSync() {
        System.out.println("begin");
        jedisSearch.dropIndex(IDX_NAME);
        Schema schema = new Schema().addSortableTextField("goodsName", 1.0).addSortableTextField("goodsIntro", 0.5).addSortableNumericField("goodsId").addSortableNumericField("sellingPrice").addSortableNumericField("goodsSellStatus").addSortableNumericField("originalPrice").addSortableTagField("tag", "|");
        jedisSearch.createIndex(IDX_NAME, Constants.GOODS_IDX_PREFIX, schema);
        List<Goods> list = goodsService.list(Wrappers.<Goods>lambdaQuery().eq(Goods::getGoodsSellStatus, 0));
        jedisSearch.addGoodsListIndex(Constants.GOODS_IDX_PREFIX, list);
        SearchResult query = jedisSearch.query(IDX_NAME, "@tag:{小米|华为} @goodsSellStatus:[0 0]", "sellingPrice");
        System.out.println("end");
    }
}
