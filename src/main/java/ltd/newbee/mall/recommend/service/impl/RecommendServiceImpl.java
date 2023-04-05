package ltd.newbee.mall.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.util.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import ltd.newbee.mall.core.dao.GoodsDao;
import ltd.newbee.mall.core.dao.OrderDao;
import ltd.newbee.mall.core.dao.OrderItemDao;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.Order;
import ltd.newbee.mall.core.entity.vo.IndexConfigGoodsVO;
import ltd.newbee.mall.core.entity.vo.OrderItemVO;
import ltd.newbee.mall.recommend.core.CoreMath;
import ltd.newbee.mall.recommend.dto.ProductDTO;
import ltd.newbee.mall.recommend.dto.RelateDTO;
import ltd.newbee.mall.recommend.service.RecommendService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RecommendServiceImpl implements RecommendService {
    private GoodsDao goodsDao;
    private OrderDao orderDao;
    private OrderItemDao orderItemDao;

    /**
     * 获取商品表所有商品id
     *
     * @return
     */
    @Override
    public List<ProductDTO> getProductData() {
        List<ProductDTO> productDTOList = new ArrayList<>();
        List<Goods> newBeeMallGoodsList = goodsDao.selectList(Wrappers.lambdaQuery());
        List<Long> goodIds = newBeeMallGoodsList.stream().map(Goods::getGoodsId).toList();
        for (Long goodId : goodIds) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(goodId.intValue());
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
        List<Order> newBeeMallOrders = orderDao.selectList(Wrappers.lambdaQuery());
        for (Order newBeeMallOrder : newBeeMallOrders) {
            List<OrderItemVO> newBeeMallOrderItems1 = orderItemDao.selectByOrderId(newBeeMallOrder.getOrderId());
            for (OrderItemVO newBeeMallOrderItem : newBeeMallOrderItems1) {
                RelateDTO relateDTO = new RelateDTO();
                relateDTO.setUserId(newBeeMallOrder.getUserId().intValue());
                relateDTO.setProductId(newBeeMallOrderItem.getGoodsId().intValue());
                // 模拟购买商品的次数(点击浏览商品的次数）
                relateDTO.setIndex(RandomUtil.randomInt(10, 9999));
                // 不能设置为1，否则皮尔森系数计算为0.0
                // relateDTO.setIndex(1);
                relateDTOList.add(relateDTO);
            }

        }
        return relateDTOList;
    }

    @Override
    public List<IndexConfigGoodsVO> recommendGoods(String userId) {
        List<IndexConfigGoodsVO> recommendGoods;
        // 协同过滤算法
        CoreMath coreMath = new CoreMath();
        // 获取商品数据
        List<RelateDTO> relateDTOList = getRelateData();
        // 执行算法，返回推荐商品id
        List<Integer> recommendIdLists = coreMath.recommend(Integer.parseInt(userId), relateDTOList);
        if (null == recommendIdLists || recommendIdLists.isEmpty()) {
            recommendGoods = null;
        } else {
            // 获取商品DTO（这里的过滤是防止商品表该id商品已下架或删除）
            List<ProductDTO> productDTOList = getProductData().stream().filter(e -> recommendIdLists.contains(e.getProductId())).toList();
            // 获取商品ids
            List<Integer> goodIds = productDTOList.stream().map(ProductDTO::getProductId).toList();
            List<Long> goodIds2 = JSONArray.parseArray(goodIds.toString(), Long.class);
            // 获取所有推荐商品
            List<Goods> newBeeMallGoods = goodsDao.selectGoodsListByIds(goodIds2);
            // 截取10个
            if (newBeeMallGoods.size() > 10) {
                newBeeMallGoods = newBeeMallGoods.stream().limit(10).collect(Collectors.toList());
            }
            // 转成VOs
            recommendGoods = BeanUtil.copyToList(newBeeMallGoods, IndexConfigGoodsVO.class);
            // 截取商品名字
            for (IndexConfigGoodsVO newBeeMallIndexConfigGoodsVO : recommendGoods) {
                String goodsName = newBeeMallIndexConfigGoodsVO.getGoodsName();
                String goodsIntro = newBeeMallIndexConfigGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 30) {
                    goodsName = goodsName.substring(0, 15) + "...";
                    newBeeMallIndexConfigGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 22) {
                    goodsIntro = goodsIntro.substring(0, 22) + "...";
                    newBeeMallIndexConfigGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        return recommendGoods;
    }

}
