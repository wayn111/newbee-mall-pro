package ltd.newbee.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.entity.ShopCat;

public interface ShopCatDao extends BaseMapper<ShopCat> {

    IPage selectListPage(Page<ShopCat> page, ShopCat shopCat);
}
