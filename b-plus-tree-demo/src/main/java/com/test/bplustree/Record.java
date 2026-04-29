package com.test.bplustree;

/**
 * 记录类：用于存储B+树叶子节点中的完整记录
 * 模拟数据库中的行记录，包含键和值
 * 
 * @author B+Tree Demo
 */
public class Record {
    
    /**
     * 记录键（4字节）
     * 用于索引和排序
     */
    private int key;
    
    /**
     * 记录值（可变长度）
     * 存储实际的数据，例如用户信息、商品详情等
     */
    private String value;
    
    /**
     * 值长度（2字节）
     * 用于序列化时确定值的大小
     */
    private int valueLength;
    
    /**
     * 构造函数
     * 
     * @param key 记录键
     * @param value 记录值
     */
    public Record(int key, String value) {
        this.key = key;
        this.value = value;
        this.valueLength = value != null ? value.length() : 0;
    }
    
    /**
     * 无参构造函数（用于反序列化）
     */
    public Record() {
    }
    
    /**
     * 获取键
     * 
     * @return 键值
     */
    public int getKey() {
        return key;
    }
    
    /**
     * 设置键
     * 
     * @param key 键值
     */
    public void setKey(int key) {
        this.key = key;
    }
    
    /**
     * 获取值
     * 
     * @return 值字符串
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 设置值
     * 
     * @param value 值字符串
     */
    public void setValue(String value) {
        this.value = value;
        this.valueLength = value != null ? value.length() : 0;
    }
    
    /**
     * 获取值长度
     * 
     * @return 值长度
     */
    public int getValueLength() {
        return valueLength;
    }
    
    /**
     * 设置值长度
     * 
     * @param valueLength 值长度
     */
    public void setValueLength(int valueLength) {
        this.valueLength = valueLength;
    }
    
    @Override
    public String toString() {
        return "Record{" +
                "key=" + key +
                ", value='" + value + '\'' +
                ", valueLength=" + valueLength +
                '}';
    }
}
