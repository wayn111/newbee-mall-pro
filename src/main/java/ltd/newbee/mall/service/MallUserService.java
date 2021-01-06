package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.MallUser;

public interface MallUserService extends IService<MallUser> {

    /**
     * 分页查询
     * @param page 分页对象
     * @param mallUser 商品用户对象
     * @return 分页数据
     */
    IPage<MallUser> selectPage(Page<MallUser> page, MallUser mallUser);
}
