package ltd.newbee.mall.core.service.impl;

import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.entity.TbTable1;
import ltd.newbee.mall.core.service.MultiDataService;
import ltd.newbee.mall.core.service.TbTable1Service;
import ltd.newbee.mall.slave.entity.TbTable2;
import ltd.newbee.mall.slave.service.TbTable2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;


@Slf4j
@Service
public class MultiDataServiceImpl implements MultiDataService {

    @Autowired
    private TbTable1Service tbTable1Service;

    @Autowired
    private TbTable2Service tbTable2Service;

    @Autowired(required = false)
    private JtaTransactionManager transactionManager;

    @Override
    public void jtaTestSuccess() {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            TbTable1 tbTable1 = new TbTable1();
            tbTable1.setName("test1");
            boolean save1 = tbTable1Service.save(tbTable1);
            TbTable2 tbTable2 = new TbTable2();
            tbTable2.setName("test2");
            boolean save2 = tbTable2Service.save(tbTable2);
            transactionManager.commit(transaction);
            Assert.isTrue(save1 && save2);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            transactionManager.rollback(transaction);
        }
    }

    @Override
    public void jtaTestRollback() {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            TbTable1 tbTable1 = new TbTable1();
            tbTable1.setName("test1");
            boolean save1 = tbTable1Service.save(tbTable1);
            TbTable2 tbTable2 = new TbTable2();
            tbTable2.setName("test2");
            boolean save2 = tbTable2Service.save(tbTable2);
            int i = 1 / 0;
            transactionManager.commit(transaction);
            Assert.isTrue(save1 && save2);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            transactionManager.rollback(transaction);
        }
    }

    @GlobalTransactional
    @Override
    public void seataTestSuccess() {
        log.info("当前 XID: {}", RootContext.getXID());
        TbTable1 tbTable1 = new TbTable1();
        tbTable1.setName("test1");
        boolean save1 = tbTable1Service.save(tbTable1);
        TbTable2 tbTable2 = new TbTable2();
        tbTable2.setName("test2");
        boolean save2 = tbTable2Service.save(tbTable2);
        Assert.isTrue(save1 && save2);
    }

    @GlobalTransactional
    @Override
    public void seataTestRollback() {
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
