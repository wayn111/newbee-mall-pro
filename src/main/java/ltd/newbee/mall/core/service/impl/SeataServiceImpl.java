package ltd.newbee.mall.core.service.impl;

import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.entity.TbTable1;
import ltd.newbee.mall.core.service.SeataService;
import ltd.newbee.mall.core.service.TbTable1Service;
import ltd.newbee.mall.slave.entity.TbTable2;
import ltd.newbee.mall.slave.service.TbTable2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class SeataServiceImpl implements SeataService {

    @Autowired
    private TbTable1Service tbTable1Service;

    @Autowired
    private TbTable2Service tbTable2Service;


    @GlobalTransactional
    @Override
    public void testTableSuccess() {
        log.info("当前 XID: {}", RootContext.getXID());
        TbTable1 tbTable1 = new TbTable1();
        tbTable1.setName("test1");
        boolean save1 = tbTable1Service.save(tbTable1);
        TbTable2 tbTable2 = new TbTable2();
        tbTable2.setName("test2");
        boolean save2 = tbTable2Service.save(tbTable2);
    }

    @GlobalTransactional
    @Override
    public void testTableRollback() {
        log.info("当前 XID: {}", RootContext.getXID());
        TbTable1 tbTable1 = new TbTable1();
        tbTable1.setName("test1");
        boolean save1 = tbTable1Service.save(tbTable1);
        TbTable2 tbTable2 = new TbTable2();
        tbTable2.setName("test2");
        boolean save2 = tbTable2Service.save(tbTable2);
        int i = 1 / 0;
    }
}
