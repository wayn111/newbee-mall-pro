package ltd.newbee.mall;


import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.service.SeckillService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SeckillExecteTest {

    @Autowired
    private SeckillService seckillService;

    private CountDownLatch countDown = new CountDownLatch(1);

    @Test
    public void test1() throws IOException {
        System.out.println("begin");
        for (int i = 0; i < 10000; i++) {
            MallUserVO userVO = new MallUserVO();
            userVO.setUserId((long) i);
            userVO.setAddress("address-test");
            new Thread(() -> {
                long begin = System.currentTimeMillis();
                try {
                    countDown.await();
                    System.out.println(seckillService.executeSeckillProcedure(16L, userVO));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    long end = System.currentTimeMillis();
                    System.out.println("executeTime: " + (end - begin));
                }
            }).start();
        }
        countDown.countDown();
        System.out.println("end");
    }
}
