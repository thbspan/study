package com.test.zookeeper.curator;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 参考 https://www.zybuluo.com/boothsun/note/990793
 */
public class CuratorClientTest {
    private CuratorFramework curatorFramework;

    @BeforeEach
    public void before() {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .retryPolicy(new RetryOneTime(1000))
                .namespace("test") // 命名空间，该curatorFramework创建的节点的父节点
                .build();
        curatorFramework.start();
    }

    /**
     * 创建节点
     */
    @Test
    public void create() throws Exception {
        // 创建一个持久化节点，初始化内容为空
        curatorFramework.create().forPath("/dus");
        // 创建一个持久化节点，初始化内容不为空
        curatorFramework.create().forPath("/dus1", "test".getBytes());
        // 创建一个临时节点  初始化内容为空
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath("/dus2");
        // 创建一个临时节点，并递归创建不存在的父节点
        // ZooKeeper中规定所有非叶子节点必须为持久节点。因此下面创建出来只有dus2会是临时节点。
        curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/dj/dus2");
    }

    /**
     * 删除节点
     */
    @Test
    public void delete() throws Exception {
        //删除一个节点
        curatorFramework.delete().forPath("/dus");
        // 删除一个节点，并递归删除其所有子节点
        curatorFramework.delete().deletingChildrenIfNeeded().forPath("/dj");
        // 删除一个节点，强制指定版本进行删除
        curatorFramework.delete().withVersion(-1).forPath("/dus1");
        //删除一个节点，强制保证删除成功
        //guaranteed()保证删除失败后，curator会在后台持续进行删除操作
        curatorFramework.delete().guaranteed().forPath("/dus2");
    }

    /**
     * 读取节点
     */
    @Test
    public void read() throws Exception {
        String path = "/";
        System.out.println(Arrays.toString(curatorFramework.getData().forPath(path)));

        Stat stat = new Stat();
        System.out.println(Arrays.toString(curatorFramework.getData().storingStatIn(stat).forPath(path)));
        System.out.println(stat);
    }

    /**
     * 更新节点
     */
    @Test
    public void update() throws Exception {
        String path = "/";
        // 更新一个节点的数据内容
        curatorFramework.setData().forPath(path, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        // 更新一个节点的数据内容，强制指定版本进行更新
        curatorFramework.setData().withVersion(-1).forPath(path, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 异步处理
     */
    @Test
    public void testAsyncThread() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        // 默认的线程池
        curatorFramework.getData().inBackground((client, event) -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println(event);
            System.out.println(client);
            countDownLatch.countDown();
        }).forPath("/trade");

        // 自定义的线程池
        Executor executor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("curator-%d").build());
        curatorFramework.getData().inBackground((client, event) -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println(event);
            System.out.println(client);
            countDownLatch.countDown();
        }, executor).forPath("/trade");
        countDownLatch.await();
    }

    /**
     * cursor事件处理，curator能够自动为开发人员处理反复注册监听
     * <br/>
     * <b>NodeCache：监听节点对应增、删、改操作</b>
     */
    @Test
    public void nodeCache() throws Exception {
        //******************** 监听一个存在的节点
        // client : Curator 客户端实例 。 path: 监听节点的节点路径 。 dataIsCompressed：是否进行数据压缩
        NodeCache nodeCache = new NodeCache(curatorFramework, "/trade", false);
        // buildInitial：如果设置为true 则NodeCache在第一次启动的时候就会立刻从ZK上读取对应节点的数据内容 保存到Cache中。
        // 调用start方法开始监听
        nodeCache.start(false);
        nodeCache.getListenable().addListener(
                () -> System.out.println("Node data update , new data:" + new String(nodeCache.getCurrentData().getData())));
        //******************** 监听一个不存在的节点 当节点被创建后，也会触发监听器 **********************//
        // client : Curator 客户端实例 。 path: 监听节点的节点路径 。 dataIsCompressed：是否进行数据压缩
        NodeCache nodeCache2 = new NodeCache(curatorFramework, "/trade1", false);
        // buildInitial：如果设置为true 则NodeCache在第一次启动的时候就会立刻从ZK上读取对应节点的数据内容 保存到Cache中。
        nodeCache2.start(false);
        nodeCache2.getListenable().addListener(
                () -> System.out.println("Node data update , new data:" + new String(nodeCache.getCurrentData().getData())));
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 测试监听子节点变化
     * <br/>
     * <b>NodeCache：监听节点下一级子节点的增、删、改操作</b>
     * <br/>
     * <b>注意事项：</b>
     * <ul>
     *     <li>无法对监听路径所在节点进行监听(即不能监听path对应节点的变化)</li>
     *     <li>只能监听path对应节点下一级目录的子节点的变化内容(即只能监听/path/node1的变化，而不能监听/path/node1/node2 的变化)</li>
     *     <li>
     *         <p>PathChildrenCache在调用start()方法时，有3种启动模式，分别为：</p>
     *         <ul>
     *             <li>NORMAL-初始化缓存数据为空</li>
     *             <li>BUILD_INITIAL_CACHE-在start方法返回前，初始化获取每个子节点数据并缓存</li>
     *             <li>POST_INITIALIZED_EVENT-在后台异步初始化数据完成后，会发送一个INITIALIZED初始化完成事件</li>
     *         </ul>
     *     </li>
     * </ul>
     */
    @Test
    public void pathChildrenCache() throws Exception {
        PathChildrenCache nodeCache = new PathChildrenCache(curatorFramework, "/trade", true);
        // buildInitial：如果设置为true 则NodeCache在第一次启动的时候就会立刻从ZK上读取对应节点的数据内容 保存到Cache中。
        // 调用start方法开始监听
        nodeCache.start();
        nodeCache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    System.out.println("新增子节点,数据内容是" + new String(event.getData().getData()));
                    break;
                case CHILD_UPDATED:
                    System.out.println("子节点被更新,数据内容是" + new String(event.getData().getData()));
                    break;
                case CHILD_REMOVED:
                    System.out.println("删除子节点,数据内容是" + new String(event.getData().getData()));
                    break;
                default:
                    break;
            }
        });
        curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath("/trade/PathChildrenCache", "new".getBytes());
        Thread.sleep(100L);
        curatorFramework.setData().forPath("/trade/PathChildrenCache", "update".getBytes());
        Thread.sleep(100L);
        curatorFramework.delete().withVersion(-1).forPath("/trade/PathChildrenCache");
    }

    /**
     * 基本同{@link #pathChildrenCache()}、{@link #nodeCache()}，
     * <br/>
     * <b>可以将指定的路径节点作为根节点，对其所有的子节点操作进行监听(包括后代节点)，呈现树形目录的监听</b>
     */
    @Test
    public void treeCache() throws Exception {
        String path = "/trade";
        TreeCache treeCache = new TreeCache(curatorFramework, path);
        // 调用start方法开始监听
        treeCache.start();
        //添加TreeCacheListener监听器
        treeCache.getListenable().addListener(
                (client, event) -> System.out.println("监听到节点数据变化，类型：" + event.getType() + ",内容：" + event.getData()));
        Thread.sleep(1000);
        //更新父节点数据
        curatorFramework.setData().forPath(path, "333".getBytes());
        Thread.sleep(1000);
        String childNodePath = path + "/child";
        //创建子节点
        curatorFramework.create().forPath(childNodePath, "111".getBytes());
        Thread.sleep(1000);
        //更新子节点
        curatorFramework.setData().forPath(childNodePath, "222".getBytes());
        Thread.sleep(1000);
        //删除子节点
        curatorFramework.delete().forPath(childNodePath);

        String subChildNodePath = childNodePath + "/child/a/b";
        curatorFramework.create().creatingParentsIfNeeded().forPath(subChildNodePath, "111".getBytes());
        Thread.sleep(1000);
        curatorFramework.setData().forPath(subChildNodePath, "222".getBytes());
        Thread.sleep(1000);
        curatorFramework.delete().forPath(subChildNodePath);
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * Master选举
     */
    @Test
    public void testLeaderSelector() throws InterruptedException {
        AtomicInteger masterCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("master_selector-%d").build());
        for (int i = 0; i < 4; i++) {
            executor.execute(() -> {
                LeaderSelector leaderSelector = new LeaderSelector(curatorFramework, "/master_selector", new LeaderSelectorListenerAdapter() {
                    @Override
                    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                        masterCount.incrementAndGet();
                        System.out.println(Thread.currentThread().getName() + "成为Master, 当前Master数量：" + masterCount);
                        Thread.sleep(1000L);
                        System.out.println(Thread.currentThread().getName() + "宕机，失去Master角色，剩下master数量：" + masterCount.decrementAndGet());
                    }
                });
                leaderSelector.autoRequeue();
                leaderSelector.start();
            });
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 分布式锁
     */
    @Test
    public void testDistributedLock() throws InterruptedException {
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/trade/mylock");
        for (int i = 0; i < 1; i++) {
            Thread currentThread = new Thread(() -> {
                try {
                    // 加锁
                    lock.acquire();
                    System.out.println(Thread.currentThread().getName() + " 抢到锁");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        System.out.println(Thread.currentThread().getName() + " 释放锁");
                        // 释放锁
                        lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            currentThread.setName("Lock【" + i + "】");
            currentThread.start();
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 分布式计数器
     */
    @Test
    public void testDistributedAtomicInteger() throws Exception {
        DistributedAtomicInteger distributedAtomicInteger = new DistributedAtomicInteger(curatorFramework,
                "/trade/PathChildrenCache", new RetryNTimes(1000, 3));
        System.out.println(distributedAtomicInteger.increment().postValue());
    }

    private DistributedBarrier distributedBarrier;

    /**
     * 分布式Barrier，没有定义成员数量。直接通过removeBarrier();释放屏障
     */
    @Test
    public void testBarrier() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("barrier-%d").build());
        for (int i = 0; i < 4; i++) {
            executor.execute(() -> {
                CuratorFramework client = CuratorFrameworkFactory.builder()
                        .connectString("master:2181,slave1:2181,slave2:2181")
                        .retryPolicy(new RetryOneTime(1000)) //重试策略
                        .namespace("zfpt") // 命名空间
                        .build();
                client.start();
                DistributedBarrier distributedBarrier = new DistributedBarrier(curatorFramework, "/trade/PathChildrenCache");
                this.distributedBarrier = distributedBarrier;
                System.out.println(Thread.currentThread().getName() + "到达Barrier前");
                try {
                    distributedBarrier.setBarrier();
                    distributedBarrier.waitOnBarrier();
                    System.out.println(Thread.currentThread().getName() + "越过屏障");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(3000L);
        //删除所有的拦截，这个相当于第5个对象，吹口哨的对象，让4个线程同时跑
        distributedBarrier.removeBarrier();
    }

    /**
     * 定义成员数量，到齐了就 越过屏障
     */
    @Test
    public void testBarrier2() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("barrier-%d").build());
        for (int i = 0; i < 4; i++) {
            executorService.execute(() -> {
                DistributedDoubleBarrier distributedDoubleBarrier = new DistributedDoubleBarrier(curatorFramework,
                        "/trade/PathChildrenCache", 4);
                try {
                    Thread.sleep(1000L);
                    System.out.println(Thread.currentThread().getName() + "到达Barrier前");
                    distributedDoubleBarrier.enter();
                    System.out.println(Thread.currentThread().getName() + "越过屏障");
                    Thread.sleep(1000L);
                    distributedDoubleBarrier.leave();
                    System.out.println(Thread.currentThread().getName() + "已经离开");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        Thread.sleep(3000L);
    }
}
