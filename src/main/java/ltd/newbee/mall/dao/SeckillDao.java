package ltd.newbee.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.entity.Seckill;
import ltd.newbee.mall.entity.vo.SeckillVO;

public interface SeckillDao extends BaseMapper<Seckill> {

    IPage selectListPage(Page page, SeckillVO seckill);

}
