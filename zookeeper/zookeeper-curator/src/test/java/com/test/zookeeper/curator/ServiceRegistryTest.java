package com.test.zookeeper.curator;

import java.util.Collection;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.test.zookeeper.curator.domain.ServerPayload;

public class ServiceRegistryTest {
    public static final String BASE_PATH = "services";
    /**
     * 测试的服务名称
     */
    public static final String SERVICE_NAME = "com.test.zookeeper.ExampleService";
    public static final String DEFAULT_URI_SPEC = "{scheme}://{address}:{port}/{name}";

    private CuratorFramework curatorFramework;

    @BeforeEach
    public void before() throws InterruptedException {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .retryPolicy(new RetryOneTime(1000))
                .namespace("demo") // 命名空间，该curatorFramework创建的节点的父节点
                .build();
        curatorFramework.start();
        curatorFramework.blockUntilConnected();
    }

    @AfterEach
    public void after() {
        curatorFramework.close();
    }

    @Test
    public void testRegistry() throws Exception {
        com.test.zookeeper.curator.ServiceRegistry serviceRegistry = new ServiceRegistry(curatorFramework, BASE_PATH);
        {
            ServiceInstance<ServerPayload> instance = ServiceInstance.<ServerPayload>builder()
                    .id("instance11")
                    .name(SERVICE_NAME)
                    .port(20880)
                    .address("192.168.1.11")
                    .payload(new ServerPayload("test", 8))
                    .uriSpec(new UriSpec(DEFAULT_URI_SPEC))
                    .build();
            System.out.println(instance.buildUriSpec());
            serviceRegistry.registerService(instance);
        }

        {
            ServiceInstance<ServerPayload> instance = ServiceInstance.<ServerPayload>builder()
                    .id("instance12")
                    .name(SERVICE_NAME)
                    .port(20880)
                    .address("192.168.1.12")
                    .payload(new ServerPayload("demo", 6))
                    .uriSpec(new UriSpec(DEFAULT_URI_SPEC))
                    .build();
            serviceRegistry.registerService(instance);
        }

        System.out.println("register service success...");

        TimeUnit.SECONDS.sleep(1);
        Collection<ServiceInstance<ServerPayload>> list = serviceRegistry.queryForInstances(SERVICE_NAME);
        if (list != null && list.size() > 0) {
            System.out.println("service:" + SERVICE_NAME + " provider list:" + new ObjectMapper().writeValueAsString(list));
        } else {
            System.out.println("service:" + SERVICE_NAME + " provider is empty...");
        }
        try (Scanner scanner = new Scanner(System.in)) {
            String line = scanner.nextLine();
            while (!"exit".equals(line)) {
                line = scanner.nextLine();
            }
        } finally {
            serviceRegistry.close();
        }
    }


}
