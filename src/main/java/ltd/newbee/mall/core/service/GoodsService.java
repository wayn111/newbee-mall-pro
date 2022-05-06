package ltd.newbee.mall.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.vo.SearchObjVO;
import ltd.newbee.mall.core.entity.vo.SearchPageGoodsVO;

import java.util.List;

public interface GoodsService extends IService<Goods> {

    /**
     * 分页查询
     *
     * @param page  分页对象
     * @param goods 商品对象
     * @return 分页数据
     */
    IPage<Goods> selectPage(Page<Goods> page, Goods goods);

    /**
     * 根据搜索条件查询商品
     *
     * @param page        分页对象
     * @param searchObjVO 搜索对象
     * @return 分页数据
     */
    IPage<Goods> findMallGoodsListBySearch(Page<SearchPageGoodsVO> page, SearchObjVO searchObjVO);

    /**
     * 同步RedisSearch
     *
     * @return boolean
     */
    boolean syncRs();

    /**
     * 保存商品信息，同步RedisSearch
     *
     * @param goods
     * @return boolean
     */
    boolean saveGoods(Goods goods);

    /**
     * 更新商品信息，同步RedisSearch
     *
     * @param goods
     * @return boolean
     */
    boolean updateGoods(Goods goods);

    /**
     * 减少商品库存
     *
     * @param goodsId
     * @param goodsCount
     */
    boolean reduceStock(Long goodsId, Integer goodsCount);

    /**
     * 修改商品上下架状态
     *
     * @param ids
     * @param sellStatus
     * @return
     */
    boolean changeSellStatus(List<Long> ids, int sellStatus);
}
