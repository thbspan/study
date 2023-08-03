package com.test.zookeeper.config;

import java.util.concurrent.ThreadLocalRandom;

class RandomOnce {
    private volatile boolean notInited = true;
    private int serverPort;

    public int nextValue(int start) {
        return nextValue(start, Integer.MAX_VALUE);
    }

    public int nextValue(int start, int end) {
        if (notInited) {
            synchronized (this) {
                if (notInited) {
                    serverPort = ThreadLocalRandom.current().nextInt(start, end);
                    notInited = false;
                }
            }
        }
        return serverPort;
    }
}
