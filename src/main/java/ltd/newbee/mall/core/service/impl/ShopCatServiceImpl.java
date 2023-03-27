package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import ltd.newbee.mall.core.dao.ShopCatDao;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.ShopCat;
import ltd.newbee.mall.core.entity.vo.ShopCatVO;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.core.service.ShopCatService;
import ltd.newbee.mall.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShopCatServiceImpl extends ServiceImpl<ShopCatDao, ShopCat> implements ShopCatService {

    private ShopCatDao shopCatDao;

    private GoodsService goodsService;

    @Override
    public void saveShopCat(ShopCat shopCat) {
        ShopCat temp = getById(shopCat.getCartItemId());
        // 购物车商品不存在
        if (temp == null) {
            temp = getOne(new QueryWrapper<ShopCat>()
                    .eq("user_id", shopCat.getUserId())
                    .eq("goods_id", shopCat.getGoodsId()));
            if (temp != null) {
                shopCat.setGoodsCount(temp.getGoodsCount());
            }
        }
        // 购物车商品已存在，修改数量
        if (temp != null) {
            Integer goodsCount = shopCat.getGoodsCount();
            temp.setGoodsCount(goodsCount != null ? goodsCount : temp.getGoodsCount() + 1);
            // 超出单个商品的最大数量
            if (temp.getGoodsCount() > 10) {
                throw new BusinessException("商品最多购买10个");
            }
            updateById(temp);
            return;
        }
        Goods goods = goodsService.getById(shopCat.getGoodsId());
        // 商品已经下架
        if (goods.getGoodsSellStatus() == 1) {
            throw new BusinessException("该商品已经下架");
        }
        // 超出单个商品的最大数量
        if (shopCat.getGoodsCount() > 10) {
            throw new BusinessException("该商品最多购买10个");
        }
        long count = count(new QueryWrapper<ShopCat>().eq("user_id", shopCat.getUserId()));
        // 购物车选购商品数量超出最大数量
        if (count > 15) {
            throw new BusinessException("购物车商品超出最大数量");
        }
        save(shopCat);
    }

    @Override
    public List<ShopCatVO> getShopCatVOList(Long userId) {
        List<ShopCatVO> collect = new ArrayList<>();
        List<ShopCat> cats = list(new QueryWrapper<ShopCat>().eq("user_id", userId));
        List<Long> goodsIds = cats.stream().map(ShopCat::getGoodsId).collect(Collectors.toList());
        if (!goodsIds.isEmpty()) {
            List<Goods> goods = goodsService.listByIds(goodsIds);
            Map<Long, Goods> goodsMap = goods.stream().collect(Collectors.toMap(Goods::getGoodsId, goodsItem -> goodsItem));
            collect.addAll(cats.stream().map(shopCat -> {
                ShopCatVO shopCatVO = new ShopCatVO();
                BeanUtils.copyProperties(shopCat, shopCatVO);
                if (goodsMap.containsKey(shopCat.getGoodsId())) {
                    Goods goods1 = goodsMap.get(shopCat.getGoodsId());
                    shopCatVO.setGoodsCoverImg(goods1.getGoodsCoverImg());
                    shopCatVO.setGoodsName(goods1.getGoodsName());
                    shopCatVO.setSellingPrice(goods1.getSellingPrice());
                }
                return shopCatVO;
            }).toList());
        }
        return collect;
    }

    @Override
    public ShopCat selectByUserIdAndGoodsId(Long userId, Long goodsId) {
        return shopCatDao.selectByUserIdAndGoodsId(userId, goodsId);
    }
}
