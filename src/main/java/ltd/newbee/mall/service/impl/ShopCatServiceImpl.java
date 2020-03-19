package ltd.newbee.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.controller.vo.ShopCatVO;
import ltd.newbee.mall.dao.ShopCatDao;
import ltd.newbee.mall.entity.Goods;
import ltd.newbee.mall.entity.ShopCat;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.service.GoodsService;
import ltd.newbee.mall.service.ShopCatService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShopCatServiceImpl extends ServiceImpl<ShopCatDao, ShopCat> implements ShopCatService {

    @Autowired
    private ShopCatDao shopCatDao;

    @Autowired
    private GoodsService goodsService;

    @Override
    public void saveShopCat(ShopCat shopCat) {
        Goods goods = goodsService.getById(shopCat.getGoodsId());
        ShopCat temp = getById(shopCat.getCartItemId());
        if (temp == null) {
            temp = getOne(new QueryWrapper<ShopCat>()
                    .eq("user_id", shopCat.getUserId())
                    .eq("goods_id", shopCat.getGoodsId()));
            if (temp != null) shopCat.setGoodsCount(temp.getGoodsCount());
        }
        if (temp != null) {
            Integer goodsCount = shopCat.getGoodsCount();
            temp.setGoodsCount(goodsCount != null ? goodsCount : temp.getGoodsCount() + 1);
            // 超出单个商品的最大数量
            if (temp.getGoodsCount() > 10) {
                throw new BusinessException("该商品最多购买10个");
            }
            updateById(temp);
            return;
        }
        // 商品已经下架
        if (goods.getGoodsSellStatus() == 1) {
            throw new BusinessException("该商品已经下架");
        }
        // 超出单个商品的最大数量
        if (shopCat.getGoodsCount() > 10) {
            throw new BusinessException("该商品最多购买10个");
        }
        int count = count(new QueryWrapper<ShopCat>().eq("user_id", shopCat.getUserId()));
        // 购物车选购商品数量超出最大数量
        if (count > 15) {
            throw new BusinessException("购物车商品超出最大数量");
        }
        save(shopCat);
    }

    @Override
    public List<ShopCatVO> getShopcatVOList(Long userId) {
        List<ShopCatVO> collect = new ArrayList<>();
        List<ShopCat> cats = list(new QueryWrapper<ShopCat>().eq("user_id", userId));
        List<Long> goodsIds = cats.stream().map(ShopCat::getGoodsId).collect(Collectors.toList());
        if (!goodsIds.isEmpty()) {
            List<Goods> goods = goodsService.listByIds(goodsIds);
            Map<Long, Goods> goodsMap = goods.stream().collect(Collectors.toMap(Goods::getGoodsId, goods1 -> goods1));
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
            }).collect(Collectors.toList()));
        }
        return collect;
    }
}
