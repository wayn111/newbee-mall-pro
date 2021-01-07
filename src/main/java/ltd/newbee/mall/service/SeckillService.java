package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.Seckill;
import ltd.newbee.mall.entity.vo.ExposerVO;
import ltd.newbee.mall.entity.vo.MallUserVO;
import ltd.newbee.mall.entity.vo.SeckillSuccessVO;
import ltd.newbee.mall.entity.vo.SeckillVO;

public interface SeckillService extends IService<Seckill> {

    /**
     * 分页查询
     *
     * @param page      分页对象
     * @param seckillVO 秒杀对象
     * @return 分页数据
     */
    IPage<Seckill> selectPage(Page<Seckill> page, SeckillVO seckillVO);

    /**
     * 执行秒杀操作
     * @param seckillId 秒杀商品ID
     * @param userVO 用户VO
     * @return 订单号
     */
    SeckillSuccessVO executeSeckill(Long seckillId, MallUserVO userVO);

    /**
     * 使用存储过程执行秒杀操作
     * @param seckillId 秒杀商品ID
     * @param userVO 用户VO
     * @return 订单号
     */
    SeckillSuccessVO executeSeckillProcedure(Long seckillId, MallUserVO userVO);

    /**
     * 秒杀地址暴露接口
     * @param seckillId 秒杀商品ID
     * @return 秒杀服务接口地址暴露类
     */
    ExposerVO exposerUrl(Long seckillId);
}
