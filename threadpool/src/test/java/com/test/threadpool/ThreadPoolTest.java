package com.test.threadpool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ThreadPoolTest {

    @Test
    void testCustomShutdownPoolPolicy() {
        Assertions.assertDoesNotThrow(() -> {
            ExecutorService executorService = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    Executors.defaultThreadFactory(),
                    // 自定义任务抛弃策略
                    (r, executor) -> r.run());

            executorService.shutdown();
            // 线程池关闭后，新添加的任务会执行任务抛弃策略
            executorService.submit(() -> System.out.println("test"));
        });
    }
}
