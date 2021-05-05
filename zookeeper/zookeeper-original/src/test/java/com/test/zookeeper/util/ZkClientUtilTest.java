package com.test.zookeeper.util;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkClientUtilTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkClientUtilTest.class);

    /**
     * 同步创建节点
     */
    @Test
    public void testCreate() throws KeeperException, InterruptedException {
        String response = ZkClientUtil.getZKConnection().create("/test", "test".getBytes(),
                // acl -> word:anyone:crwda
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        LOGGER.info(response);
    }

    /**
     * 异步回调创建 zk节点
     */
    @Test
    public void createASync() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //StringCallback 异步回调  ctx:用于传递给回调方法的一个参数。通常是放一个上下文(Context)信息
        ZkClientUtil.getZKConnection().create("/test2", "test".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, (returnCode, path, ctx, name) -> {
                    LOGGER.info("rc:{}, path={}, ctx={}", returnCode, path, ctx);
                    countDownLatch.countDown();
                }, "1212121");
        countDownLatch.await();
    }

    /**
     * 同步删除
     */
    @Test
    public void delete() throws Exception {
        // version 表示此次删除针对于的版本号。 版本号一致才会删除，-1可以绕过这个机制，即不管数据的版本号而将它直接删除
        ZkClientUtil.getZKConnection().delete("/test", -1);
    }

    /**
     * 异步删除
     */
    @Test
    public void deleteASync() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZkClientUtil.getZKConnection().delete("/aa3", -1, (returnCode, path, ctx) -> {
            LOGGER.info("rc:{}, path={}, ctx={}", returnCode, path, ctx);
            countDownLatch.countDown();
        }, "删除操作");
        countDownLatch.await();
    }

    /**
     * 同步获取数据，包括子节点列表的获取和当前节点数据的获取
     */
    @Test
    public void getChildren() throws Exception {
        Stat stat = new Stat();
        // path:指定数据节点的节点路径， 即API调用的目的是获取该节点的子节点列表
        // Watcher : 注册的Watcher。一旦在本次获取子节点之后，子节点列表发生变更的话，就会向该Watcher发送通知。Watcher仅会被触发一次。
        // state: 获取指定数据节点(也就是path参数对应的节点)的状态信息(无节点名和数据内容)，传入旧的state将会被来自服务端响应的新state对象替换。
        List<String> list = ZkClientUtil.getZKConnection().getChildren("/",
                event -> LOGGER.info("我是监听事件，监听子节点变化{}", event), stat);
        LOGGER.info("child={}", list);
        LOGGER.info("path state={}", stat);
        Thread.sleep(5_000);
    }

    /**
     * 异步获取子节点
     */
    @Test
    public void getChildrenASync() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZkClientUtil.getZKConnection().getChildren("/",
                event -> LOGGER.info("我是监听事件，监听子节点变化"), (rc, path, ctx, children) -> {
                    //异步回调
                    LOGGER.info("children={}", children);
                    countDownLatch.countDown();
                }, "context");
        countDownLatch.await();
    }

    /**
     * 同步获取数据
     */
    @Test
    public void getDataTest() throws Exception {
        Stat stat = new Stat();
        byte[] bytes = ZkClientUtil.getZKConnection().getData("/dubbo",
                event -> LOGGER.info("我是监听事件，监听数据状态发生变化"), stat);
        System.out.println(new String(bytes));
    }

    /**
     * 异步获取数据
     */
    @Test
    public void getDataASync() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZkClientUtil.getZKConnection().getData("/dubbo",
                event -> LOGGER.info("我是监听事件，监听数据状态发生变化"),
                (rc, path, ctx, data, stat) -> {
                    LOGGER.info("获取到的内容是:{}", new String(data, StandardCharsets.UTF_8));
                    countDownLatch.countDown();
                }, "121");
        countDownLatch.await();
    }

    /**
     * 同步更新数据
     */
    @Test
    public void setData() throws Exception {
        byte[] oldValue = ZkClientUtil.getZKConnection().getData("/test", false, null);
        LOGGER.info("更新前值是：{}", new String(oldValue, StandardCharsets.UTF_8));
        Stat stat = ZkClientUtil.getZKConnection().setData("/test", "helloWorld".getBytes(), -1);
        LOGGER.info("更新后：stat={}", stat);
        byte[] newValue = ZkClientUtil.getZKConnection().getData("/test", false, null);
        LOGGER.info("更新后值是：{}", new String(newValue, StandardCharsets.UTF_8));
    }

    /**
     * 异步更新数据
     */
    @Test
    public void setDataASync() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZkClientUtil.getZKConnection().setData("/test", "helloChina".getBytes(), -1,
                (rc, path, ctx, stat) -> {
                    LOGGER.info("更新后：stat={}", stat);
                    countDownLatch.countDown();
                }, "1111");
        countDownLatch.await();
        byte[] newValue = ZkClientUtil.getZKConnection().getData("/test", false, null);
        LOGGER.info("更新后的值是：{}", new String(newValue, StandardCharsets.UTF_8));
    }
}
