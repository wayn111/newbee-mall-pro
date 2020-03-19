package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.MallUser;

public interface MallUserService extends IService<MallUser> {
    IPage selectPage(Page<MallUser> page, MallUser mallUser);
}
