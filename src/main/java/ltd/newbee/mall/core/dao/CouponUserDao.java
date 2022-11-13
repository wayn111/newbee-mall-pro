package ltd.newbee.mall.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ltd.newbee.mall.core.entity.CouponUser;

import java.util.List;

public interface CouponUserDao extends BaseMapper<CouponUser> {
    List<CouponUser> selectAvailableList(Long userId);
}
