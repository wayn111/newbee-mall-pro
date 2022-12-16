package ltd.newbee.mall;


import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.service.MultiDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class MultiDataSourceTest {

    @Autowired
    private MultiDataService multiDataService;

    @Test
    public void testRollback() {
        multiDataService.testRollback();
    }


    @Test
    public void jtaTestSuccess() {
        multiDataService.jtaTestSuccess();
    }

    @Test
    public void jtaTestRollback() {
        multiDataService.jtaTestRollback();
    }

    @Test
    public void seataTestSuccess() {
        multiDataService.seataTestSuccess();
    }

    @Test
    public void seataTestRollback() {
        multiDataService.seataTestRollback();
    }
}
