package ltd.newbee.mall;


import ltd.newbee.mall.entity.vo.MallUserVO;
import ltd.newbee.mall.service.SeckillService;
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

    private CountDownLatch begin = new CountDownLatch(3000);

    @Test
    public void test1() throws InterruptedException, IOException {
        System.out.println("begin");
        for (int i = 0; i < 3000; i++) {
            MallUserVO userVO = new MallUserVO();
            userVO.setUserId((long) i);
            userVO.setAddress("address-test");
            new Thread(() -> {
                begin.countDown();
                System.out.println(seckillService.executeSeckill(1L, userVO));
            }).start();
        }
        begin.await();
        System.out.println("end");
        System.in.read();
    }
}
