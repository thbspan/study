package com.test.job.config;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobLogFileCleanThread;
import com.xxl.job.core.thread.TriggerCallbackThread;

public class CustomXxlJobSpringExecutor extends XxlJobSpringExecutor {
    private String logPath;
    private int logRetentionDays;
    private String adminAddresses;
    private String accessToken;

    @Override
    public void afterSingletonsInstantiated() {
        try {
            // init JobHandler Repository (for method)
            Method initJobHandlerMethodRepository = XxlJobSpringExecutor.class.getDeclaredMethod("initJobHandlerMethodRepository", ApplicationContext.class);
            initJobHandlerMethodRepository.setAccessible(true);
            initJobHandlerMethodRepository.invoke(this, getApplicationContext());

            // refresh GlueFactory
            GlueFactory.refreshInstance(1);
            start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {
        // init logpath
        XxlJobFileAppender.initLogPath(logPath);

        // init JobLogFileCleanThread
        JobLogFileCleanThread.getInstance().start(logRetentionDays);

        // init invoker, admin-client
        Method initAdminBizList = XxlJobExecutor.class.getDeclaredMethod("initAdminBizList", String.class, String.class);
        initAdminBizList.setAccessible(true);
        initAdminBizList.invoke(this, adminAddresses, accessToken);

        // init TriggerCallbackThread
        TriggerCallbackThread.getInstance().start();
    }

    @Override
    public void setLogPath(String logPath) {
        this.logPath = logPath;
        super.setLogPath(logPath);
    }

    @Override
    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
        super.setLogRetentionDays(logRetentionDays);
    }

    @Override
    public void setAdminAddresses(String adminAddresses) {
        this.adminAddresses = adminAddresses;
        super.setAdminAddresses(adminAddresses);
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        super.setAccessToken(accessToken);
    }
}
