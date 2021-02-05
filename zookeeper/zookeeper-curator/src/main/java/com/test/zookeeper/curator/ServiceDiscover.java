package com.test.zookeeper.curator;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;

import com.test.zookeeper.curator.domain.ServerPayload;

public class ServiceDiscover {
    private final ServiceDiscovery<ServerPayload> serviceDiscovery;
    private final ConcurrentHashMap<String, ServiceProvider<ServerPayload>> serviceProviderMap = new ConcurrentHashMap<>();

    public ServiceDiscover(CuratorFramework curatorFramework, String basePath) {
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServerPayload.class)
                .client(curatorFramework)
                .basePath(basePath)
                .serializer(new JsonInstanceSerializer<>(ServerPayload.class))
                .build();
    }

    public ServiceInstance<ServerPayload> getServiceProvider(String serviceName) throws Exception {
        ServiceProvider<ServerPayload> provider = serviceProviderMap.get(serviceName);
        if (provider == null) {
            provider = serviceDiscovery.serviceProviderBuilder()
                    .serviceName(serviceName)
                    .providerStrategy(new RandomStrategy<>())
                    .build();

            ServiceProvider<ServerPayload> oldProvider = serviceProviderMap.putIfAbsent(serviceName, provider);
            if (oldProvider != null) {
                provider = oldProvider;
            } else {
                provider.start();
            }
        }

        return provider.getInstance();
    }

    public void start() throws Exception {
        serviceDiscovery.start();
    }

    public void close() throws IOException {
        for (Map.Entry<String, ServiceProvider<ServerPayload>> me : serviceProviderMap.entrySet()){
            try{
                me.getValue().close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        serviceDiscovery.close();
    }
}
