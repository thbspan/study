package com.test.zookeeper.curator;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.x.discovery.ServiceInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.test.zookeeper.curator.domain.ServerPayload;

public class ServiceDiscoverTest {

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
    public void testDiscover() throws Exception {
        // 服务发线
        ServiceDiscover serviceDiscover = new ServiceDiscover(curatorFramework, ServiceRegistryTest.BASE_PATH);
        try {
            serviceDiscover.start();
            for (int i = 0; i < 10; i++) {
                ServiceInstance<ServerPayload> instance = serviceDiscover.getServiceProvider(ServiceRegistryTest.SERVICE_NAME);

                System.out.println("service:" + ServiceRegistryTest.SERVICE_NAME + " instance id:" + instance.getId() +
                        ", name:" + instance.getName() + ", address:" + instance.getAddress() + ", port:" + instance.getPort());

                TimeUnit.SECONDS.sleep(1);
            }
        } finally {
            serviceDiscover.close();
        }
    }
}
