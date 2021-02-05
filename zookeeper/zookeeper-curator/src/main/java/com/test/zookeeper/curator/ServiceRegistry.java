package com.test.zookeeper.curator;

import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import com.test.zookeeper.curator.domain.ServerPayload;

public class ServiceRegistry {
    private final ServiceDiscovery<ServerPayload> serviceDiscovery;

    public ServiceRegistry(CuratorFramework curatorFramework, String basePath) {
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServerPayload.class)
                .client(curatorFramework)
                .serializer(new JsonInstanceSerializer<>(ServerPayload.class))
                .basePath(basePath)
                .build();
    }

    public void updateService(ServiceInstance<ServerPayload> instance) throws Exception {
        serviceDiscovery.updateService(instance);
    }

    public void registerService(ServiceInstance<ServerPayload> instance) throws Exception {
        serviceDiscovery.registerService(instance);
    }

    public void unregisterService(ServiceInstance<ServerPayload> instance) throws Exception {
        serviceDiscovery.unregisterService(instance);
    }

    public Collection<ServiceInstance<ServerPayload>> queryForInstances(String name) throws Exception {
        return serviceDiscovery.queryForInstances(name);
    }

    public ServiceInstance<ServerPayload> queryForInstance(String name, String id) throws Exception {
        return serviceDiscovery.queryForInstance(name, id);
    }

    public void start() throws Exception {
        serviceDiscovery.start();
    }

    public void close() throws Exception {
        serviceDiscovery.close();
    }
}
