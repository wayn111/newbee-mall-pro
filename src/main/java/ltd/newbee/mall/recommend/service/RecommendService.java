package ltd.newbee.mall.recommend.service;

import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.vo.IndexConfigGoodsVO;
import ltd.newbee.mall.recommend.dto.ProductDTO;
import ltd.newbee.mall.recommend.dto.RelateDTO;

import java.util.List;

public interface RecommendService {

    List<ProductDTO> getProductData();

    List<RelateDTO> getRelateData();

    List<Goods> recommendGoods(Long userId, Integer num);
}
