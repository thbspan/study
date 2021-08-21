package com.test.job.config;

import javax.annotation.Resource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.test.job.service.DemoJobHandler;
import com.xxl.job.core.executor.XxlJobExecutor;

@Component
public class XxlJobBeanModeRegistryConfig implements ApplicationRunner {

    @Resource
    private DemoJobHandler demoJobHandler;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        XxlJobExecutor.registJobHandler("demoJobHandler", demoJobHandler);
    }
}
