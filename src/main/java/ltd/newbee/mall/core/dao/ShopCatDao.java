package ltd.newbee.mall.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.core.entity.ShopCat;

public interface ShopCatDao extends BaseMapper<ShopCat> {

    IPage<ShopCat> selectListPage(Page<ShopCat> page, ShopCat shopCat);

    ShopCat selectByUserIdAndGoodsId(Long userId, Long goodsId);
}
