package ltd.newbee.mall.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.vo.SearchObjVO;
import ltd.newbee.mall.core.entity.vo.SearchPageGoodsVO;

public interface GoodsDao extends BaseMapper<Goods> {
    IPage<Goods> selectListPage(Page<Goods> page, Goods goods);

    IPage<Goods> findMallGoodsListBySearch(Page<SearchPageGoodsVO> page, SearchObjVO searchObjVO);

    boolean addStock(Long goodsId, Integer number);
}
