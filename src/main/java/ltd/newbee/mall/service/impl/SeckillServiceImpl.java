package ltd.newbee.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.dao.SeckillDao;
import ltd.newbee.mall.entity.Seckill;
import ltd.newbee.mall.entity.SeckillSuccess;
import ltd.newbee.mall.entity.vo.ExposerVO;
import ltd.newbee.mall.entity.vo.MallUserVO;
import ltd.newbee.mall.entity.vo.SeckillSuccessVO;
import ltd.newbee.mall.entity.vo.SeckillVO;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.service.SeckillService;
import ltd.newbee.mall.service.SeckillSuccessService;
import ltd.newbee.mall.util.security.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillDao, Seckill> implements SeckillService {

    public static final String SECKILL_KEY = "seckillId:";
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SeckillSuccessService seckillSuccessService;
    @Autowired
    private RedisCache redisCache;

    @Override
    public IPage<Seckill> selectPage(Page<Seckill> page, SeckillVO seckillVO) {
        return seckillDao.selectListPage(page, seckillVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SeckillSuccessVO executeSeckill(Long seckillId, MallUserVO userVO) {
        Seckill seckill = getById(seckillId);
        int count = seckillSuccessService.count(new QueryWrapper<SeckillSuccess>()
                .eq("seckill_id", seckillId)
                .eq("user_id", userVO.getUserId()));
        if (count >= seckill.getLimitNum()) {
            throw new BusinessException("您的购买数量已经超出秒杀限购数量");
        }
        if (seckill.getSeckillNum() <= 0) {
            throw new BusinessException("秒杀商品已售空");
        }
        // 判断秒杀商品是否再有效期内
        long beginTime = seckill.getSeckillBegin().getTime();
        long endTime = seckill.getSeckillEnd().getTime();
        Date now = new Date();
        long nowTime = now.getTime();
        if (nowTime < beginTime) {
            throw new BusinessException("秒杀未开启");
        } else if (nowTime > endTime) {
            throw new BusinessException("秒杀已结束");
        }
        // 执行秒杀逻辑：减库存 + 记录购买行为
        if (!seckillDao.reduceNumber(seckillId, now.getTime() / 1000)) {
            throw new BusinessException("秒杀商品减库存失败");
        }
        SeckillSuccess seckillSuccess = new SeckillSuccess();
        seckillSuccess.setSeckillId(seckillId);
        seckillSuccess.setUserId(userVO.getUserId());
        seckillSuccess.setStatus((byte) 1);
        if (!seckillSuccessService.save(seckillSuccess)) {
            throw new BusinessException("保存用户秒杀商品失败");
        }
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_EXECUTE_SALT));
        return seckillSuccessVO;
    }

    @Override
    public ExposerVO exposerUrl(Long seckillId) {
        Seckill seckill = redisCache.getCacheObject(SECKILL_KEY + seckillId);
        if (seckill == null) {
            seckill = getById(seckillId);
            redisCache.setCacheObject(SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
        }
        Date startTime = seckill.getSeckillBegin();
        Date endTime = seckill.getSeckillEnd();
        // 系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new ExposerVO(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        // 加密
        String md5 = Md5Utils.hash(seckillId);
        return new ExposerVO(true, md5, seckillId);
    }
}
