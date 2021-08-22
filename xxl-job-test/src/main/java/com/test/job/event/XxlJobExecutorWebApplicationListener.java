package com.test.job.event;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.test.job.config.XxlJobConfig;
import com.xxl.job.core.thread.ExecutorRegistryThread;
import com.xxl.job.core.util.IpUtil;

@Component
public class XxlJobExecutorWebApplicationListener implements ApplicationListener<WebServerInitializedEvent> {
    private final XxlJobConfig xxlJobConfig;

    public XxlJobExecutorWebApplicationListener(XxlJobConfig xxlJobConfig) {
        this.xxlJobConfig = xxlJobConfig;
    }


    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        String ip = xxlJobConfig.getIp();
        ip = (ip != null && ip.trim().length() > 0) ? ip : IpUtil.getIp();
        String ip_port_address = IpUtil.getIpPort(ip, port);
        String address = "http://{ip_port}/xxl".replace("{ip_port}", ip_port_address);
        // start registry
        ExecutorRegistryThread.getInstance().start(xxlJobConfig.getAppname(), address);
    }
}
