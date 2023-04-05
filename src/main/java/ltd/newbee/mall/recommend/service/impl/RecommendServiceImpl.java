package ltd.newbee.mall.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.dao.GoodsDao;
import ltd.newbee.mall.core.dao.OrderDao;
import ltd.newbee.mall.core.dao.OrderItemDao;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.vo.OrderItemVO;
import ltd.newbee.mall.recommend.core.CoreMath;
import ltd.newbee.mall.recommend.dto.ProductDTO;
import ltd.newbee.mall.recommend.dto.RelateDTO;
import ltd.newbee.mall.recommend.service.RecommendService;
import ltd.newbee.mall.redis.JedisSearch;
import ltd.newbee.mall.util.MyBeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.springframework.stereotype.Service;
import redis.clients.jedis.search.SearchResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class RecommendServiceImpl implements RecommendService {
    private GoodsDao goodsDao;
    private OrderDao orderDao;
    private OrderItemDao orderItemDao;
    private JedisSearch jedisSearch;

    /**
     * 获取商品表所有商品id
     *
     * @return
     */
    @Override
    public List<ProductDTO> getProductData() {
        List<ProductDTO> productDTOList = new ArrayList<>();
        SearchResult query = jedisSearch.queryAll(Constants.GOODS_IDX_NAME, "*", null);
        List<Goods> newBeeMallGoodsList = query.getDocuments().stream().map(document -> {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> property : document.getProperties()) {
                String key = property.getKey();
                Object value = property.getValue();
                map.put(key, value);
            }
            return MyBeanUtil.toBean(map, Goods.class);
        }).toList();

        List<Long> goodIds = newBeeMallGoodsList.stream().map(Goods::getGoodsId).toList();
        for (Long goodId : goodIds) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(goodId);
            productDTOList.add(productDTO);
        }
        return productDTOList;
    }

    /**
     * 获取所有用户购买商品的记录
     *
     * @return
     */
    @Override
    public List<RelateDTO> getRelateData() {
        List<RelateDTO> relateDTOList = new ArrayList<>();
        List<Order> newBeeMallOrders = orderDao.selectOrderIds();
        List<Long> orderIds = newBeeMallOrders.stream().map(Order::getOrderId).toList();
        List<OrderItemVO> newBeeMallOrderItems = orderItemDao.selectByOrderIds(orderIds);
        Map<Long, List<OrderItemVO>> listMap = newBeeMallOrderItems.stream().collect(Collectors.groupingBy(OrderItemVO::getOrderId));
        Map<Long, List<OrderItemVO>> goodsListMap = newBeeMallOrderItems.stream().collect(Collectors.groupingBy(OrderItemVO::getGoodsId));
        for (Order newBeeMallOrder : newBeeMallOrders) {
            Long orderId = newBeeMallOrder.getOrderId();
            for (OrderItemVO newBeeMallOrderItem : listMap.getOrDefault(orderId, Collections.emptyList())) {
                Long goodsId = newBeeMallOrderItem.getGoodsId();
                Long categoryId = newBeeMallOrderItem.getCategoryId();
                RelateDTO relateDTO = new RelateDTO();
                relateDTO.setUserId(newBeeMallOrder.getUserId());
                relateDTO.setProductId(goodsId);
                relateDTO.setCategoryId(categoryId);
                // 模拟购买商品的次数(点击浏览商品的次数）
                // relateDTO.setIndex(RandomUtil.randomInt(10, 9999));
                List<OrderItemVO> list = goodsListMap.getOrDefault(goodsId, Collections.emptyList());
                int sum = list.stream().mapToInt(OrderItemVO::getGoodsCount).sum();
                relateDTO.setIndex(sum);
                // 不能设置为1，否则皮尔森系数计算为0.0
                // relateDTO.setIndex(1);
                relateDTOList.add(relateDTO);
            }
        }
        return relateDTOList;
    }

    @Override
    public List<Goods> recommendGoods(Long userId, Integer num) {
        List<Goods> recommendGoods = new ArrayList<>();
        // 协同过滤算法
        CoreMath coreMath = new CoreMath();
        // 获取商品数据
        List<RelateDTO> relateDTOList = getRelateData();
        // 执行算法，返回推荐商品id
        List<Long> recommendIdLists = coreMath.recommend(userId, num, relateDTOList);
        Map<Long, Integer> integerMap = IntStream.range(0, recommendIdLists.size()).boxed().collect(Collectors.toMap(recommendIdLists::get, i -> i));
        if (CollectionUtils.isNotEmpty(recommendIdLists)) {
            // 获取商品DTO（这里的过滤是防止商品表该id商品已下架或删除）
            List<ProductDTO> productDTOList = getProductData().stream().filter(e -> recommendIdLists.contains(e.getProductId())).toList();
            // 获取商品ids
            List<Long> goodIds = productDTOList.stream().map(ProductDTO::getProductId).toList();
            if (CollectionUtils.isNotEmpty(goodIds)) {
                // 获取所有推荐商品
                recommendGoods = goodsDao.selectGoodsListByIds(goodIds);
                recommendGoods.sort(Comparator.comparingInt(o -> integerMap.get(o.getGoodsId())));
            }
        }
        return recommendGoods.stream().limit(num).toList();
    }

    public static void main(String[] args) {
        RandomUtil.getRandom().ints(5);
        System.out.printf(Arrays.toString(IntStream.range(0, 10).toArray()));
        System.out.println(Stream.of(10).toList());
    }

}
