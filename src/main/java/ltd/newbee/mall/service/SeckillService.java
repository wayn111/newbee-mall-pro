package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.Seckill;

public interface SeckillService extends IService<Seckill> {

    /**
     * 分页查询
     * @param page 分页对象
     * @param seckill 秒杀对象
     * @return 分页数据
     */
    IPage selectPage(Page<Seckill> page, Seckill seckill);
}
