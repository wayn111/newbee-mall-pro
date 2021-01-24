package ltd.newbee.mall.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.core.entity.MallUser;

public interface MallUserDao extends BaseMapper<MallUser> {

    IPage<MallUser> selectListPage(Page<MallUser> page, MallUser mallUser);
}
