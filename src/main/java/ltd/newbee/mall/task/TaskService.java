package ltd.newbee.mall.task;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Slf4j
@Component
public class TaskService {
    private final DelayQueue<Task> delayQueue = new DelayQueue<>();

    @PostConstruct
    private void init() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNamePrefix("task-pool-%d").build();
        // 通用线程池
        ExecutorService pool = new ThreadPoolExecutor(5, 20,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    Task task = delayQueue.take();
                    task.run();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void addTask(Task task) {
        if (delayQueue.contains(task)) {
            return;
        }
        delayQueue.add(task);
    }

    public void removeTask(Task task) {
        delayQueue.remove(task);
    }

}
