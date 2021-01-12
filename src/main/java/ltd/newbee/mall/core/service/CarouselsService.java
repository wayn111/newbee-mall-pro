package ltd.newbee.mall.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ltd.newbee.mall.core.entity.Carousels;

public interface CarouselsService extends IService<Carousels> {

    /**
     * 分页查询
     * @param page 分页对象
     * @param carousels 轮播图对象
     * @return 分页数据
     */
    IPage<Carousels> selectPage(Page<Carousels> page, Carousels carousels);
}
