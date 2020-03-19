package ltd.newbee.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.entity.Carousels;

public interface CarouselsService extends IService<Carousels> {

    IPage selectPage(Page<Carousels> page, Carousels carousels);
}
