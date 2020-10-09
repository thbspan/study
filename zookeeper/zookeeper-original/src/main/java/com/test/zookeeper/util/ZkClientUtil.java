package com.test.zookeeper.util;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkClientUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkClientUtil.class);

    private static final String connectString = "localhost:2181";

    private static final int SESSION_TIME_OUT = 1000;

    private static ZooKeeper zooKeeper;

    private static final CountDownLatch connectedSemaphore = new CountDownLatch(1);

    static {
        try {
            zooKeeper = new ZooKeeper(connectString, SESSION_TIME_OUT, event -> {
                LOGGER.info("已经触发了事件！event={}", event);
                connectedSemaphore.countDown();
            });
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static ZooKeeper getZKConnection() {
        try {
            if (zooKeeper == null) {
                connectedSemaphore.await();
            }
            return zooKeeper;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IllegalStateException(e);
        }
    }


}
