package com.test.arthas.instrument.collector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CostTimeCollector {
    private static final ConcurrentHashMap<String, Map<String, Long>> COST_TIME_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    /**
     * @see com.test.arthas.instrument.transformer.CostTimeTransformer#transform
     */
    public static void addCostTime(Thread thread, StackTraceElement[] stackTrace, long time) {
        String[] keys = getUniqueKey(thread, stackTrace);
        Map<String, Long> map = COST_TIME_CONCURRENT_HASH_MAP.get(keys[0]);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        map.put(keys[1], time);
        COST_TIME_CONCURRENT_HASH_MAP.put(keys[0], map);
    }

    public static ConcurrentHashMap<String, Map<String, Long>> getCostTime() {
        return COST_TIME_CONCURRENT_HASH_MAP;
    }

    private static String[] getUniqueKey(Thread thread, StackTraceElement[] stackTrace) {
        String s = thread.getId() + "@" + Thread.currentThread().getName();
        String s1 = stackTrace[1].getClassName() + "@" + stackTrace[1].getMethodName();
        return new String[]{s, s1, s + s1};
    }
}
