package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.core.dao.MallUserDao;
import ltd.newbee.mall.core.entity.MallUser;
import ltd.newbee.mall.core.service.MallUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MallUserServiceImpl extends ServiceImpl<MallUserDao, MallUser> implements MallUserService {

    @Autowired
    private MallUserDao mallUserDao;

    @Override
    public IPage<MallUser> selectPage(Page<MallUser> page, MallUser mallUser) {
        return mallUserDao.selectListPage(page, mallUser);
    }
}
