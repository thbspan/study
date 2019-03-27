package com.test.threadpool;

        import java.util.concurrent.ExecutorService;
        import java.util.concurrent.Executors;
        import java.util.concurrent.LinkedBlockingQueue;
        import java.util.concurrent.ThreadPoolExecutor;
        import java.util.concurrent.TimeUnit;

        import org.junit.Test;

public class ThreadPoolTest {

    @Test
    public void testShutdownPoolPolicy() {
        ExecutorService executorService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                Executors.defaultThreadFactory(), (r, executor) -> r.run());
        executorService.shutdown();

        executorService.submit(() -> System.out.println("test"));
    }
}
