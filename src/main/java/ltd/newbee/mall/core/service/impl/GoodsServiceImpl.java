package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.core.dao.GoodsDao;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.vo.SearchObjVO;
import ltd.newbee.mall.core.entity.vo.SearchPageGoodsVO;
import ltd.newbee.mall.core.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsDao, Goods> implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    @Override
    public IPage<Goods> selectPage(Page<Goods> page, Goods goods) {
        return goodsDao.selectListPage(page, goods);
    }

    @Override
    public IPage<Goods> findMallGoodsListBySearch(Page<SearchPageGoodsVO> page, SearchObjVO searchObjVO) {
        return goodsDao.findMallGoodsListBySearch(page, searchObjVO);
    }

}
