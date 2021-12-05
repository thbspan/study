package com.test.mybatis.plus.task;

@FunctionalInterface
public interface Job extends Runnable {

    /**
     * 执行任务接口
     */
    void run();
}
