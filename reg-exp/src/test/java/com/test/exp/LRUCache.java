package com.test.exp;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {
    private final int capacity;

    private final Map<K, V> cacheMap;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        cacheMap = new LinkedHashMap<>(capacity, 0.75f, true);
    }

    public V get(K key) {
        return cacheMap.get(key);
    }

    public void put(K key, V value) {
        cacheMap.put(key, value);
        if (cacheMap.size() > capacity) {
            K firstKey = cacheMap.keySet().iterator().next();
            cacheMap.remove(firstKey);
        }
    }

    public Iterator<K> keyIterator() {
        return cacheMap.keySet().iterator();
    }

    private static <K, V> void println(LRUCache<K, V> cache) {
        for (Iterator<K> it = cache.keyIterator(); it.hasNext(); ) {
            System.out.print(it.next() + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        LRUCache<String, String> cache = new LRUCache<>(5);
        cache.put("1", "a");
        cache.put("2", "b");
        cache.put("3", "c");
        cache.put("4", "d");
        cache.put("5", "e");

        println(cache);
        cache.put("6","e");
        println(cache);
        System.out.println(cache.get("3"));
        println(cache);
        System.out.println(cache.keyIterator().next());
    }
}
