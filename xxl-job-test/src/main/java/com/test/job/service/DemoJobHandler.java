package com.test.job.service;

import org.springframework.stereotype.Component;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;

@Component
public class DemoJobHandler extends IJobHandler {

    @Override
    public void execute() throws Exception {
        XxlJobHelper.log("XXL-JOB: Bean Mode.");
    }
}
