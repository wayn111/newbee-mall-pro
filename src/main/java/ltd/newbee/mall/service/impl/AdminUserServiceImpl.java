package ltd.newbee.mall.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.dao.AdminUserDao;
import ltd.newbee.mall.entity.AdminUser;
import ltd.newbee.mall.service.AdminUserService;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserDao, AdminUser> implements AdminUserService {
}
