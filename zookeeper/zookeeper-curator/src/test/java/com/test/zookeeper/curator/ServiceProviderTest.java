package com.test.zookeeper.curator;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.test.zookeeper.curator.domain.ServerPayload;

public class ServiceProviderTest {
    public static final String BASE_PATH = "services";
    /**
     * 示例服务名称
     */
    public static final String SERVICE_NAME = "com.test.service.ExampleService";

    private CuratorFramework curatorFramework;

    @BeforeEach
    public void before() {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .retryPolicy(new RetryOneTime(1000))
                .build();
        curatorFramework.start();
    }

    @Test
    public void testRegistry() throws Exception {
        ServiceRegistry serviceRegistry = new ServiceRegistry(curatorFramework, BASE_PATH);

        ServiceInstance<ServerPayload> instance1 = ServiceInstance.<ServerPayload>builder()
                .id("exampleInstance1")
                .name(SERVICE_NAME)
                .port(21880)
                .address("localhost")
                .payload(new ServerPayload("HZ", 5))
                .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                .build();

        serviceRegistry.registerService(instance1);
        System.out.println("register service success...");

        TimeUnit.SECONDS.sleep(1);

        Collection<ServiceInstance<ServerPayload>> list = serviceRegistry.queryForInstances(SERVICE_NAME);
        if (list != null && list.size() > 0) {
            System.out.println("service:" + SERVICE_NAME + " provider list:" + list);
        } else {
            System.out.println("service:" + SERVICE_NAME + " provider is empty...");
        }
    }
}
