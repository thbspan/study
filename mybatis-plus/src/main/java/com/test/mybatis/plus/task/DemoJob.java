package com.test.mybatis.plus.task;

import java.util.concurrent.ScheduledExecutorService;

public class DemoJob extends BaseJob{

    public DemoJob(ScheduledExecutorService scheduledExecutorService) {
        super(scheduledExecutorService);
    }
}
