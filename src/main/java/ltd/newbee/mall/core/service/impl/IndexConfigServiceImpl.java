package ltd.newbee.mall.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jdk.jshell.JShell;
import ltd.newbee.mall.core.dao.IndexConfigDao;
import ltd.newbee.mall.core.entity.Goods;
import ltd.newbee.mall.core.entity.IndexConfig;
import ltd.newbee.mall.core.service.GoodsService;
import ltd.newbee.mall.core.service.IndexConfigService;
import ltd.newbee.mall.enums.IndexConfigTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndexConfigServiceImpl extends ServiceImpl<IndexConfigDao, IndexConfig> implements IndexConfigService {

    @Resource
    private IndexConfigDao indexConfigDao;

    @Resource
    private GoodsService goodsService;

    @Override
    public IPage<IndexConfig> selectPage(Page<IndexConfig> page, IndexConfig indexConfig) {
        return indexConfigDao.selectListPage(page, indexConfig);
    }

    @Override
    public List<Goods> listIndexConfig(IndexConfigTypeEnum indexGoodsHot, int limit) {
        List<IndexConfig> list = list(new QueryWrapper<IndexConfig>()
                .eq("config_type", indexGoodsHot.getType())
                .last("limit " + limit)
                .orderByDesc("config_rank"));
        List<Long> collect = list.stream().map(IndexConfig::getGoodsId).collect(Collectors.toList());
        return goodsService.listByIds(collect);
    }

    public static void main(String[] args) {
        JShell shell = JShell.create();
        shell.eval("int a=10;");
        shell.eval("int b=10;");
        var eval = shell.eval("a+b;");
        System.out.println(eval.get(0).value());
    }
}
