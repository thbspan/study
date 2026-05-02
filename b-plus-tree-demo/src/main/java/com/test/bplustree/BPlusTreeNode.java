package com.test.bplustree;

import java.util.LinkedList;
import java.util.List;

/**
 * B+树节点类
 * B+树是一种自平衡的树数据结构，能够保持数据有序
 * 这种数据结构常用于数据库和文件系统的索引
 */
public class BPlusTreeNode {

    /**
     * 是否为叶子节点
     * B+树的特点：所有数据都存储在叶子节点中
     * 非叶子节点只起到索引作用
     */
    boolean isLeaf;

    /**
     * 节点中的键值列表
     * 叶子节点：存储实际的数据键
     * 非叶子节点：存储用于索引的键
     */
    List<Integer> keys;

    /**
     * 子节点列表（仅非叶子节点使用）
     * 子节点数量总是比键的数量多1
     */
    List<BPlusTreeNode> children;

    /**
     * 指向下一个叶子节点的指针（仅叶子节点使用）
     * 用于支持范围查询和顺序访问
     */
    BPlusTreeNode nextLeaf;

    /**
     * 父节点引用（可选，用于某些操作）
     */
    BPlusTreeNode parent;

    // ==================== 持久化相关字段 ====================
    /**
     * 页号（用于持久化）
     * 标识该节点在数据文件中的页位置
     */
    private int pageNum = -1;

    /**
     * 父节点页号（用于持久化）
     * 替代 parent 对象引用
     */
    private int parentPageNum = -1;

    /**
     * 子节点页号列表（用于持久化）
     * 替代 children 对象列表
     */
    private final List<Integer> childPageNums = new LinkedList<>();

    /**
     * 下一个叶子页号（用于持久化）
     * 替代 nextLeaf 对象引用
     */
    private int nextLeafPageNum = -1;

    /**
     * 前一个叶子页号（用于持久化）
     * 支持双向链表遍历
     */
    private int prevLeafPageNum = -1;

    /**
     * 记录列表（用于持久化）
     * 叶子节点存储完整的记录（键+值）
     */
    private final List<TreeRecord> records = new LinkedList<>();

    /**
     * 构造函数：创建一个新的B+树节点
     *
     * @param isLeaf 是否为叶子节点
     */
    public BPlusTreeNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new LinkedList<>();
        this.children = new LinkedList<>();
        this.nextLeaf = null;
        this.parent = null;
    }

    /**
     * 获取键的数量
     *
     * @return 键的数量
     */
    public int getKeyCount() {
        return keys.size();
    }

    /**
     * 判断节点是否为空
     *
     * @return 如果节点为空返回true
     */
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    /**
     * 在指定位置插入键
     *
     * @param index 插入位置
     * @param key 要插入的键
     */
    public void insertKey(int index, int key) {
        keys.add(index, key);
    }

    /**
     * 在指定位置插入子节点
     *
     * @param index 插入位置
     * @param child 要插入的子节点
     */
    public void insertChild(int index, BPlusTreeNode child) {
        children.add(index, child);
    }

    /**
     * 移除指定位置的键
     *
     * @param index 要移除的键的位置
     * @return 被移除的键
     */
    public int removeKey(int index) {
        return keys.remove(index);
    }

    /**
     * 移除指定位置的子节点
     *
     * @param index 要移除的子节点的位置
     * @return 被移除的子节点
     */
    public BPlusTreeNode removeChild(int index) {
        return children.remove(index);
    }

    /**
     * 获取指定位置的键
     *
     * @param index 键的位置
     * @return 指定位置的键
     */
    public int getKey(int index) {
        return keys.get(index);
    }

    /**
     * 获取指定位置的子节点
     *
     * @param index 子节点的位置
     * @return 指定位置的子节点
     */
    public BPlusTreeNode getChild(int index) {
        return children.get(index);
    }

    /**
     * 设置指定位置的键
     *
     * @param index 键的位置
     * @param key 新的键值
     */
    public void setKey(int index, int key) {
        keys.set(index, key);
    }

    @Override
    public String toString() {
        return "BPlusTreeNode{" +
                "isLeaf=" + isLeaf +
                ", keys=" + keys +
                '}';
    }

    // ==================== 持久化相关方法 ====================

    /**
     * 获取页号
     *
     * @return 页号
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * 设置页号
     *
     * @param pageNum 页号
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 获取父节点页号
     *
     * @return 父节点页号
     */
    public int getParentPageNum() {
        return parentPageNum;
    }

    /**
     * 设置父节点页号
     *
     * @param parentPageNum 父节点页号
     */
    public void setParentPageNum(int parentPageNum) {
        this.parentPageNum = parentPageNum;
    }

    /**
     * 获取子节点页号列表
     *
     * @return 子节点页号列表
     */
    public List<Integer> getChildPageNums() {
        return childPageNums;
    }

    /**
     * 添加子节点页号
     *
     * @param childPageNum 子节点页号
     */
    public void addChildPageNum(int childPageNum) {
        childPageNums.add(childPageNum);
    }

    /**
     * 插入子节点页号
     *
     * @param index 插入位置
     * @param childPageNum 子节点页号
     */
    public void insertChildPageNum(int index, int childPageNum) {
        childPageNums.add(index, childPageNum);
    }

    /**
     * 移除子节点页号
     *
     * @param index 移除位置
     * @return 被移除的子节点页号
     */
    public int removeChildPageNum(int index) {
        return childPageNums.remove(index);
    }

    /**
     * 获取指定位置的子节点页号
     *
     * @param index 位置
     * @return 子节点页号
     */
    public int getChildPageNum(int index) {
        return childPageNums.get(index);
    }

    /**
     * 获取下一个叶子页号
     *
     * @return 下一个叶子页号
     */
    public int getNextLeafPageNum() {
        return nextLeafPageNum;
    }

    /**
     * 设置下一个叶子页号
     *
     * @param nextLeafPageNum 下一个叶子页号
     */
    public void setNextLeafPageNum(int nextLeafPageNum) {
        this.nextLeafPageNum = nextLeafPageNum;
    }

    /**
     * 获取前一个叶子页号
     *
     * @return 前一个叶子页号
     */
    public int getPrevLeafPageNum() {
        return prevLeafPageNum;
    }

    /**
     * 设置前一个叶子页号
     *
     * @param prevLeafPageNum 前一个叶子页号
     */
    public void setPrevLeafPageNum(int prevLeafPageNum) {
        this.prevLeafPageNum = prevLeafPageNum;
    }

    /**
     * 获取记录列表
     *
     * @return 记录列表
     */
    public List<TreeRecord> getRecords() {
        return records;
    }

    /**
     * 添加记录
     *
     * @param record 记录
     */
    public void addRecord(TreeRecord record) {
        records.add(record);
        // 同时添加到keys列表（保持兼容）
        keys.add(record.key());
    }

    /**
     * 插入记录
     *
     * @param index 插入位置
     * @param record 记录
     */
    public void insertRecord(int index, TreeRecord record) {
        records.add(index, record);
        keys.add(index, record.key());
    }

    /**
     * 移除记录
     *
     * @param index 移除位置
     * @return 被移除的记录
     */
    public TreeRecord removeRecord(int index) {
        keys.remove(index);
        return records.remove(index);
    }

    /**
     * 添加键（用于内部节点）
     *
     * @param key 键
     */
    public void addKey(int key) {
        keys.add(key);
    }
}
