package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.util.concurrent.RateLimiter;
import lombok.AllArgsConstructor;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.dao.SeckillDao;
import ltd.newbee.mall.core.entity.Seckill;
import ltd.newbee.mall.core.entity.SeckillSuccess;
import ltd.newbee.mall.core.entity.vo.ExposerVO;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.entity.vo.SeckillSuccessVO;
import ltd.newbee.mall.core.entity.vo.SeckillVO;
import ltd.newbee.mall.core.service.SeckillService;
import ltd.newbee.mall.core.service.SeckillSuccessService;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.redis.RedisCache;
import ltd.newbee.mall.util.security.Md5Utils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class SeckillServiceImpl extends ServiceImpl<SeckillDao, Seckill> implements SeckillService {

    // 使用令牌桶RateLimiter 限流
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(10);

    private SeckillDao seckillDao;
    private SeckillSuccessService seckillSuccessService;
    private RedisCache redisCache;

    @Override
    public IPage<Seckill> selectPage(Page<Seckill> page, SeckillVO seckillVO) {
        return seckillDao.selectListPage(page, seckillVO);
    }

    @Override
    public ExposerVO exposerUrl(Long seckillId) {
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (seckill == null) {
            seckill = getById(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
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

    @Override
    public boolean addStock(Long seckillId) {
        return seckillDao.addStock(seckillId);
    }

    /**
     * 1、判断用户是否买过<br>
     * 2、判断商品库存是否大于0<br>
     * 3、判断秒杀商品是否再有效期内<br>
     * 4、执行秒杀逻辑：减库存 + 记录购买行为<br>
     * 5、返回用户秒杀成功VO
     *
     * @param seckillId 秒杀商品ID
     * @param userVO    用户VO
     * @return 用户秒杀成功VO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SeckillSuccessVO executeSeckill(Long seckillId, MallUserVO userVO) {
        // 判断用户是否买过
        long count = seckillSuccessService.count(new QueryWrapper<SeckillSuccess>()
                .eq("seckill_id", seckillId)
                .eq("user_id", userVO.getUserId()));
        if (count > 0) {
            throw new BusinessException("您已经购买过秒杀商品，请勿重复购买");
        }
        Seckill seckill = getById(seckillId);
        // 查询秒杀商品库存
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
        if (!reduceStock(seckillId, now)) {
            throw new BusinessException("秒杀商品减库存失败");
        }
        SeckillSuccess seckillSuccess = new SeckillSuccess();
        seckillSuccess.setSeckillId(seckillId);
        seckillSuccess.setUserId(userVO.getUserId());
        if (!seckillSuccessService.save(seckillSuccess)) {
            throw new BusinessException("保存用户秒杀商品失败");
        }
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_EXECUTE_SALT));
        return seckillSuccessVO;
    }

    /**
     * 1、判断用户是否买过<br>
     * 2、使用redis原子自减，判断商品缓存库存是否大于0<br>
     * 3、获取商品缓存，判断秒杀商品是否再有效期内<br>
     * 4、执行执行存储过程（减库存 + 记录购买行为）<br>
     * 5、返回用户秒杀成功VO
     *
     * @param seckillId 秒杀商品ID
     * @param userVO    用户VO
     * @return 用户秒杀成功VO
     */
    @Override
    public SeckillSuccessVO executeSeckillProcedure(Long seckillId, MallUserVO userVO) {
        // 判断用户是否购买过秒杀商品
        long count = seckillSuccessService.count(new QueryWrapper<SeckillSuccess>()
                .eq("seckill_id", seckillId)
                .eq("user_id", userVO.getUserId()));
        if (count > 0) {
            throw new BusinessException("您已经购买过秒杀商品，请勿重复购买");
        }
        // 更新秒杀商品库存
        Long stock = redisCache.luaDecrement(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            throw new BusinessException("秒杀商品已售空");
        }
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (seckill == null) {
            seckill = getById(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
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

        Date killTime = new Date();
        Long userId = userVO.getUserId();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("userId", userId);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行存储过程，result被赋值
        killByProcedure(map);
        // 获取result -2sql执行失败 -1未插入数据 0未更新数据 1sql执行成功
        int result = MapUtils.getInteger(map, "result", -2);
        if (result != 1) {
            throw new BusinessException("很遗憾！未抢购到秒杀商品");
        }
        SeckillSuccess seckillSuccess = seckillSuccessService.getOne(new QueryWrapper<SeckillSuccess>()
                .eq("seckill_id", seckillId)
                .eq("user_id", userId));
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_EXECUTE_SALT));
        return seckillSuccessVO;
    }

    /**
     * 1、使用令牌桶算法过滤用户请求<br>
     * 2、使用redis-set数据结构判断用户是否买过秒杀商品<br>
     * 3、使用redis原子自减，判断商品缓存库存是否大于0<br>
     * 4、获取商品缓存，判断秒杀商品是否再有效期内<br>
     * 5、执行执行存储过程（减库存 + 记录购买行为）<br>
     * 6、使用redis-set数据结构记录购买过的用户<br>
     * 7、返回用户秒杀成功VO
     *
     * @param seckillId 秒杀商品ID
     * @param userVO    用户VO
     * @return 用户秒杀成功VO
     */
    @Override
    public SeckillSuccessVO executeSeckillLimiting(Long seckillId, MallUserVO userVO) {
        // 判断能否在500毫秒内得到令牌，如果不能则立即返回false，不会阻塞程序
        if (!RATE_LIMITER.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            // System.out.println("短期无法获取令牌，真不幸，排队也瞎排");
            throw new BusinessException("秒杀失败");
        }
        // 判断用户是否购买过秒杀商品
        if (redisCache.containsCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userVO.getUserId())) {
            throw new BusinessException("您已经购买过秒杀商品，请勿重复购买");
        }
        // 更新秒杀商品库存
        Long stock = redisCache.luaDecrement(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            throw new BusinessException("秒杀商品已售空");
        }
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (seckill == null) {
            seckill = getById(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
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

        Date killTime = new Date();
        Long userId = userVO.getUserId();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("userId", userId);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行存储过程，result被赋值
        killByProcedure(map);
        // 获取result -2sql执行失败 -1未插入数据 0未更新数据 1sql执行成功
        int result = MapUtils.getInteger(map, "result", -2);
        if (result != 1) {
            throw new BusinessException("很遗憾！未抢购到秒杀商品");
        }
        // 记录购买过的用户
        redisCache.setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userVO.getUserId());
        long endExpireTime = endTime / 1000;
        long nowExpireTime = nowTime / 1000;
        redisCache.expire(Constants.SECKILL_SUCCESS_USER_ID + seckillId, endExpireTime - nowExpireTime, TimeUnit.SECONDS);
        SeckillSuccess seckillSuccess = seckillSuccessService.getOne(new QueryWrapper<SeckillSuccess>()
                .eq("seckill_id", seckillId)
                .eq("user_id", userId));
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_EXECUTE_SALT));
        return seckillSuccessVO;
    }


    /**
     * 秒杀最终方案
     * 1、使用令牌桶算法过滤用户请求<br>
     * 2、使用redis-set数据结构判断用户是否买过秒杀商品<br>
     * 3、返回用户秒杀成功VO
     *
     * @param seckillId 秒杀商品ID
     * @param userVO    秒杀用户VO
     * @return SeckillSuccessVO
     */
    public SeckillSuccessVO executeSeckillFinal(Long seckillId, MallUserVO userVO) {
        // 判断能否在500毫秒内得到令牌，如果不能则立即返回false，不会阻塞程序
        if (!RATE_LIMITER.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            // System.out.println("短期无法获取令牌，真不幸，排队也瞎排");
            throw new BusinessException("秒杀失败");
        }
        // 判断用户是否购买过秒杀商品
        if (redisCache.containsCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userVO.getUserId())) {
            throw new BusinessException("您已经购买过秒杀商品，请勿重复购买");
        }
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (seckill == null) {
            seckill = getById(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
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

        SeckillSuccess seckillSuccess = new SeckillSuccess();
        seckillSuccess.setSeckillId(seckillId);
        seckillSuccess.setUserId(userVO.getUserId());
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
    public void killByProcedure(Map<String, Object> paramMap) {
        seckillDao.killByProcedure(paramMap);
    }

    @Override
    public boolean reduceStock(Long seckillId, Date now) {
        return seckillDao.reduceStock(seckillId, now);
    }
}
