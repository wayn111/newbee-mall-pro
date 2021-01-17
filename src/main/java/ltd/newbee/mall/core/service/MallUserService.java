package ltd.newbee.mall.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.core.entity.MallUser;

public interface MallUserService extends IService<MallUser> {

    /**
     * 分页查询
     *
     * @param page     分页对象
     * @param mallUser 商品用户对象
     * @return 分页数据
     */
    IPage<MallUser> selectPage(Page<MallUser> page, MallUser mallUser);

    /**
     * 用户注册
     *
     * @param loginName 用户名
     * @param password  密码
     * @return boolean
     */
    boolean register(String loginName, String password);
}
