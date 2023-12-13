// package ltd.newbee.mall.task;
//
// import ltd.newbee.mall.util.ThreadUtil;
// import ltd.newbee.mall.util.spring.SpringContextUtil;
// import org.springframework.boot.ApplicationArguments;
// import org.springframework.boot.ApplicationRunner;
// import org.springframework.context.event.SimpleApplicationEventMulticaster;
// import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
// import org.springframework.stereotype.Component;
//
// import java.util.concurrent.*;
//
// /**
//  * 系统启动时执行
//  */
// @Component
// public class SpringBeanStartupRunner implements ApplicationRunner {
//
//     @Override
//     public void run(ApplicationArguments args) throws Exception {
//         // 设置spring默认的事件监听为异步执行
//         SimpleApplicationEventMulticaster multicaster = SpringContextUtil.getBean(SimpleApplicationEventMulticaster.class);
//         ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
//         multicaster.setTaskExecutor(executorService);
//         Runtime.getRuntime().addShutdownHook(new Thread(() -> ThreadUtil.shutdownAndAwaitTermination(executorService)));
//
//     }
// }
