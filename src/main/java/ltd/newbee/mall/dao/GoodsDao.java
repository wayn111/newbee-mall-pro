package ltd.newbee.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.entity.Goods;
import ltd.newbee.mall.entity.vo.SearchObjVO;
import ltd.newbee.mall.entity.vo.SearchPageGoodsVO;

public interface GoodsDao extends BaseMapper<Goods> {
    IPage selectListPage(Page<Goods> page, Goods goods);

    IPage findMallGoodsListBySearch(Page<SearchPageGoodsVO> page, SearchObjVO searchObjVO);
}
