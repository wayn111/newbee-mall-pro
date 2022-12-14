package ltd.newbee.mall;


import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.entity.TbTable1;
import ltd.newbee.mall.core.service.SeataService;
import ltd.newbee.mall.core.service.TbTable1Service;
import ltd.newbee.mall.slave.entity.TbTable2;
import ltd.newbee.mall.slave.service.TbTable2Service;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class MultiDataSourceTest {

    @Autowired
    private TbTable1Service tbTable1Service;

    @Autowired
    private TbTable2Service tbTable2Service;

    // @Autowired
    // private JtaTransactionManager transactionManager;

    @Autowired(required = false)
    private JtaTransactionManager transactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private SeataService seataService;


    @Test
    public void jtaTest() {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            TbTable1 tbTable1 = new TbTable1();
            tbTable1.setName("test1");
            boolean save1 = tbTable1Service.save(tbTable1);
            TbTable2 tbTable2 = new TbTable2();
            tbTable2.setName("test2");
            boolean save2 = tbTable2Service.save(tbTable2);
            System.out.println(1 / 0);
            transactionManager.commit(transaction);
            Assert.assertTrue(save1 && save2);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            transactionManager.rollback(transaction);
        }
    }

    @Test
    public void seataXATest() {
        seataService.testTableSuccess();
    }

    @Test
    public void seataXATestRollback() {
        seataService.testTableRollback();
    }
}
