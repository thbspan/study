package com.test.mybatis.plus.task;

import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.SmartLifecycle;

public abstract class BaseJob implements Job, SmartLifecycle {
    /**
     * 任务集合key前缀
     */
    private static final String REDIS_TASK_KEY_PREFIX = "__reuse_file:";

    /**
     * 任务锁key前缀
     */
    private static final String REDIS_TASK_LOCK_KEY_PREFIX = REDIS_TASK_KEY_PREFIX + "lock:";

    private boolean running = false;

    private final ScheduledExecutorService scheduledExecutorService;

    public BaseJob(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public void fetchPendingJob() {
        // 获取待处理的任务放到set集合中
    }

    public void handlerExceptionJob() {
        // 处理有异常的任务
    }

    @Override
    public void run() {
        // 获取任务
        // 检查任务状态
        // tryMarkJobRunning 尝试将任务设置为 Running
        // doJob 执行实际的任务
        // markJobCompletedAndMoveToHistory 标记任务执行成功并移动到历史表
        // markTaskError 标记任务执行失败
    }

    @Override
    public void start() {
        // 启动 fetchPendingJob 定时任务
        // 启动 handlerExceptionJob 定时任务
        // 多线程并发执行 run 方法
        this.running = true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void stop() {
        this.running = false;
    }
}
