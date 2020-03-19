package ltd.newbee.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ltd.newbee.mall.dao.OrderItemDao;
import ltd.newbee.mall.entity.OrderItem;
import ltd.newbee.mall.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItem> implements OrderItemService {
}
