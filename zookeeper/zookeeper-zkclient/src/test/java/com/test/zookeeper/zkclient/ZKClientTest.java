package com.test.zookeeper.zkclient;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ZKClientTest {
    private static final String serverString = "localhost:2181";

    private ZkClient zkClient;

    @BeforeEach
    public void init() {
        zkClient = new ZkClient(serverString, 10_000, 10_000);
    }

    /**
     * 创建节点
     */
    @Test
    public void testCreate() {
        // 创建节点
        String result = zkClient.create("/aa3", "test", CreateMode.EPHEMERAL);
        System.out.println(result);
        // 递归创建
        zkClient.createPersistent("/trade/open", true);
    }

    /**
     * 删除节点
     */
    @Test
    public void testDelete() {
        System.out.println("非递归删除结果:" + zkClient.delete("/trade"));
        // 递归删除
        System.out.println("递归删除结果:" + zkClient.deleteRecursive("/trade"));
    }

    /**
     * 读取子节点
     */
    @Test
    public void testGetChildren() {
        System.out.println(zkClient.getChildren("/dubbo"));
    }

    /**
     * 读取节点数据
     */
    @Test
    public void testReadData() {
        System.out.println((String) zkClient.readData("/dubbo"));
    }

    /**
     * 更新数据
     */
    @Test
    public void testSetData() {
        String oldValue = zkClient.readData("/trade");
        System.out.println("获取前:" + oldValue);
        zkClient.writeData("/trade", "I am trade");
        String newValue = zkClient.readData("/trade");
        System.out.println("更新后:" + newValue);
    }

    /**
     * 监听子节点和数据变化事件，ZkClient的listener已经支持重复注册
     */
    @Test
    public void testSubscribe() {
        //监听子节点变化
        zkClient.subscribeChildChanges("/trade",
                (parentPath, currenChilds) -> System.out.println("子节点发生变化"));
        // 监听节点数据变化
        zkClient.subscribeDataChanges("/trade", new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                System.out.println("dataPath:" + dataPath + "发生变化，最新数据是:" + data);
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                System.out.println("dataPath被删除");
            }
        });
    }
}