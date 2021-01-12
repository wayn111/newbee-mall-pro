package ltd.newbee.mall.core.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.core.dao.AdminUserDao;
import ltd.newbee.mall.core.entity.AdminUser;
import ltd.newbee.mall.core.service.AdminUserService;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserDao, AdminUser> implements AdminUserService {
}
