package ltd.newbee.mall.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.core.entity.Carousels;

public interface CarouselsDao extends BaseMapper<Carousels> {

    IPage<Carousels> selectListPage(Page<Carousels> page, Carousels carousels);
}
