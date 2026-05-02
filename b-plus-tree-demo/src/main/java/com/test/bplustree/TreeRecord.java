package com.test.bplustree;

/**
 * 记录类：用于存储B+树叶子节点中的完整记录
 * 模拟数据库中的行记录，包含键和值
 *
 * @author B+Tree Demo
 */
public record TreeRecord(int key, String value) {

}
