package com.test.bplustree;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 缓冲池：简单的LRU页缓存机制
 * 用于提高频繁访问页的性能，减少磁盘I/O
 *
 */
public class BufferPool {
    /**
     * 页缓存：使用LinkedHashMap实现LRU
     * key: 页号, value: 页数据
     */
    private final Map<Integer, byte[]> cache;

    /**
     * 脏页集合：记录被修改过的页
     * key: 页号, value: 页数据
     */
    private final Map<Integer, byte[]> dirtyPages;

    /**
     * 最大缓存页数
     */
    private final int maxSize;

    /**
     * 构造函数
     *
     * @param maxSize 最大缓存页数
     */
    public BufferPool(int maxSize) {
        this.maxSize = maxSize;
        this.dirtyPages = new LinkedHashMap<>();

        // 使用LinkedHashMap实现LRU：accessOrder=true表示按访问顺序排序
        this.cache = new LinkedHashMap<Integer, byte[]>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
                // 当缓存满时，移除最久未使用的页
                return size() > BufferPool.this.maxSize;
            }
        };
    }

    /**
     * 获取页
     * 如果页在缓存中直接返回，否则从磁盘读取
     *
     * @param pageNum     页号
     * @param pageManager 页管理器
     * @return 页数据
     * @throws Exception 如果读取失败
     */
    public byte[] getPage(int pageNum, PageManager pageManager) throws Exception {
        // 1. 先检查脏页
        if (dirtyPages.containsKey(pageNum)) {
            return dirtyPages.get(pageNum);
        }

        // 2. 检查缓存
        if (cache.containsKey(pageNum)) {
            return cache.get(pageNum);
        }

        // 3. 从磁盘读取
        byte[] pageData = pageManager.readPage(pageNum);

        // 4. 添加到缓存
        cache.put(pageNum, pageData);

        return pageData;
    }

    /**
     * 将页添加到缓存
     *
     * @param pageNum  页号
     * @param pageData 页数据
     */
    public void putPage(int pageNum, byte[] pageData) {
        cache.put(pageNum, pageData);
    }

    /**
     * 将页标记为脏页（需要写回磁盘）
     *
     * @param pageNum  页号
     * @param pageData 修改后的页数据
     */
    public void markDirty(int pageNum, byte[] pageData) {
        dirtyPages.put(pageNum, pageData);
        // 同时更新缓存
        cache.put(pageNum, pageData);
    }

    /**
     * 刷新所有脏页到磁盘
     *
     * @param pageManager 页管理器
     * @throws Exception 如果写入失败
     */
    public void flushAll(PageManager pageManager) throws Exception {
        for (Map.Entry<Integer, byte[]> entry : dirtyPages.entrySet()) {
            int pageNum = entry.getKey();
            byte[] pageData = entry.getValue();

            // 写入磁盘
            pageManager.writePage(pageNum, pageData);
        }

        // 清空脏页集合
        dirtyPages.clear();
    }

    /**
     * 刷新指定页到磁盘
     *
     * @param pageNum     页号
     * @param pageManager 页管理器
     * @throws Exception 如果写入失败
     */
    public void flushPage(int pageNum, PageManager pageManager) throws Exception {
        if (dirtyPages.containsKey(pageNum)) {
            byte[] pageData = dirtyPages.remove(pageNum);
            pageManager.writePage(pageNum, pageData);
        }
    }

    /**
     * 获取缓存中的页数
     *
     * @return 缓存页数
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * 获取脏页数量
     *
     * @return 脏页数量
     */
    public int getDirtyPageCount() {
        return dirtyPages.size();
    }

    /**
     * 清空缓存
     */
    public void clear() {
        cache.clear();
        dirtyPages.clear();
    }
}
