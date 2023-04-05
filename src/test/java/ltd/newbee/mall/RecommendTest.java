package ltd.newbee.mall;

import com.alibaba.fastjson2.JSONArray;
import ltd.newbee.mall.recommend.core.CoreMath;
import ltd.newbee.mall.recommend.dto.ProductDTO;
import ltd.newbee.mall.recommend.dto.RelateDTO;
import ltd.newbee.mall.recommend.service.RecommendService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NewBeeMallApplication.class)
public class RecommendTest {
    @Autowired
    private RecommendService recommendService;

    @Test
    public void getProductData() {
        List<ProductDTO> productData = recommendService.getProductData();
        System.out.println("---------------size = " + productData.size());
        for (ProductDTO productDatum : productData) {
            System.out.println(productDatum);
        }
    }

    @Test
    public void getRelateData() {
        List<RelateDTO> relateData = recommendService.getRelateData();
        System.out.println("relateDTO size=" + relateData.size());
        for (RelateDTO relateDatum : relateData) {
            System.out.println(relateData);
        }

        List<Integer> integers = Arrays.asList(6, 4, 3, 2, 1, 5, 9, 8, 7);
        // 默认从小到大排序
        Collections.sort(integers);
        // 逆转排序
        integers.sort(Comparator.reverseOrder());
        List<Integer> limit = integers.stream().limit(3).toList();
        limit.forEach(System.out::println);
    }


    /**
     * 测试推荐商品功能
     */
    @Test
    public void recommendGoods() {
        List<ProductDTO> productData = recommendService.getProductData();
        // 协同过滤算法
        CoreMath coreMath = new CoreMath();
        // 获取商品数据
        List<RelateDTO> relateData = recommendService.getRelateData();
        // 执行算法，返回推荐商品id
        List<Integer> recommendIdLists = coreMath.recommend(901, relateData);

        System.out.println("recommendIdLists size = " + recommendIdLists.size());
        for (Integer recommendIdList : recommendIdLists) {
            System.out.println(recommendIdList);
        }

        // 获取商品DTO
        List<ProductDTO> productDTOList = productData.stream().filter(e -> recommendIdLists.contains(e.getProductId())).toList();
        System.out.println("productDTOList size=" + productDTOList.size());

        for (ProductDTO productDTO : productDTOList) {
            System.out.println(productDTO.getProductId());
        }
    }
}
