package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.controller.vo.ShopCatVO;
import ltd.newbee.mall.entity.ShopCat;

import java.util.List;

public interface ShopCatService extends IService<ShopCat> {
    void saveShopCat(ShopCat shopCat);

    List<ShopCatVO> getShopcatVOList(Long userId);
}
