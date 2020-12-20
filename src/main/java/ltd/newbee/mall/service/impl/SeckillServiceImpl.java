package ltd.newbee.mall.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.dao.SeckillDao;
import ltd.newbee.mall.entity.Seckill;
import ltd.newbee.mall.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillDao, Seckill> implements SeckillService {

    @Autowired
    private SeckillDao seckillDao;

    @Override
    public IPage selectPage(Page<Seckill> page, Seckill seckill) {
        return seckillDao.selectListPage(page, seckill);
    }
}
