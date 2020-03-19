package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.controller.vo.SearchObjVO;
import ltd.newbee.mall.controller.vo.SearchPageGoodsVO;
import ltd.newbee.mall.entity.Goods;

public interface GoodsService extends IService<Goods> {

    IPage selectPage(Page<Goods> page, Goods goods);

    IPage findMallGoodsListBySearch(Page<SearchPageGoodsVO> page, SearchObjVO searchObjVO);

}
