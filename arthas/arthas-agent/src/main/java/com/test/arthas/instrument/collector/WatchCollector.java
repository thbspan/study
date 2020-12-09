package com.test.arthas.instrument.collector;

import java.util.concurrent.ConcurrentHashMap;

import com.test.arthas.instrument.advice.Advice;


public class WatchCollector {
    public static void add(String key, Advice advice) throws Exception {
        for (WatchListener value : LISTENERS.values()) {
            value.call(key, advice);
        }
    }

    private static final ConcurrentHashMap<String, WatchListener> LISTENERS = new ConcurrentHashMap<>();

    public static void addListener(String key, WatchListener watchListener) {
        LISTENERS.put(key, watchListener);
    }

    public static void removeListener(String key) {
        LISTENERS.remove(key);
    }
}
