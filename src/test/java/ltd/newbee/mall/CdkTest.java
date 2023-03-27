package ltd.newbee.mall;


import cn.hutool.core.collection.ListUtil;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.core.entity.CdkInfo;
import ltd.newbee.mall.core.service.CdkInfoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class CdkTest {

    @Autowired
    private CdkInfoService cdkInfoService;

    /**
     * cdk创建
     */
    @Test
    public void cdkCreate() {
        Integer num = 100000;
        List<CdkInfo> list = new ArrayList<>(num);
        Date date = new Date();
        String createUser = "test";
        for (Integer i = 0; i < num; i++) {
            CdkInfo temp = new CdkInfo();
            temp.setCdkNo(String.valueOf(i));
            temp.setCreateTime(date);
            temp.setCreateUser(createUser);
            list.add(temp);
        }
        long begin = System.currentTimeMillis();
        boolean flag = false;
        for (List<CdkInfo> cdkInfos : ListUtil.partition(list, 1000)) {
            flag = cdkInfoService.saveBatch(cdkInfos, cdkInfos.size());
            if (!flag) {
                break;
            }
        }
        long end = System.currentTimeMillis();
        log.info("执行耗时：" + (end - begin) + "ms");
        Assert.isTrue(flag, "批量更新失败");
    }

    /**
     * 通过线程池创建cdk
     */
    @Test
    public void asyncCdkCreate() {
        int num = 100000;
        List<CdkInfo> list = new ArrayList<>(num);
        Date date = new Date();
        String createUser = "test";
        for (Integer i = 0; i < num; i++) {
            CdkInfo temp = new CdkInfo();
            temp.setCdkNo(String.valueOf(i));
            temp.setCreateTime(date);
            temp.setCreateUser(createUser);
            list.add(temp);
        }
        long begin = System.currentTimeMillis();
        List<Boolean> flagList = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<CdkInfo> cdkInfos : ListUtil.partition(list, 1000)) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                boolean b = cdkInfoService.saveBatch(cdkInfos, cdkInfos.size());
                flagList.add(b);
            }, ForkJoinPool.commonPool());
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long end = System.currentTimeMillis();
        log.info("执行耗时：" + (end - begin) + "ms");
        Assert.isTrue(flagList.stream().filter(aBoolean -> !aBoolean).findFirst().orElse(true), "批量更新失败");
    }

}
