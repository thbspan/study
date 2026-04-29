package com.test.bplustree;

import java.util.List;

/**
 * B+树测试类
 * 用于演示和测试B+树的各种操作
 * 
 * @author B+Tree Demo
 */
public class BPlusTreeDemo {
    
    static void main() {
        System.out.println("=== B+树学习演示 ===\n");
        
        // 创建一个阶数为3的B+树
        // 阶数为3表示每个节点最多有3个子节点，最多存储2个键
        BPlusTree tree = new BPlusTree(3);
        System.out.println("创建了一个阶数为3的B+树");
        System.out.println("每个节点最多存储 " + (tree.getOrder() - 1) + " 个键");
        System.out.println();
        
        // 演示插入操作
        System.out.println("--- 插入操作演示 ---");
        int[] keys = {10, 20, 5, 6, 12, 30, 7, 17};
        
        for (int key : keys) {
            System.out.println("插入: " + key);
            tree.insert(key);
            System.out.println("树的高度: " + tree.getHeight());
            System.out.println("树的结构:");
            tree.printTree();
            System.out.println();
        }
        
        // 验证所有键是否有序存储
        System.out.println("--- 验证有序性 ---");
        List<Integer> allKeys = tree.getAllKeys();
        System.out.println("所有键（按顺序）: " + allKeys);
        System.out.println("是否有序: " + isSorted(allKeys));
        System.out.println();
        
        // 演示查找操作
        System.out.println("--- 查找操作演示 ---");
        int[] searchKeys = {5, 10, 15, 20, 25};
        for (int key : searchKeys) {
            boolean found = tree.search(key);
            System.out.println("查找 " + key + ": " + (found ? "找到" : "未找到"));
        }
        System.out.println();
        
        // 演示B+树的特点
        System.out.println("--- B+树特点说明 ---");
        System.out.println("1. 所有数据都存储在叶子节点中");
        System.out.println("2. 非叶子节点只起到索引作用");
        System.out.println("3. 叶子节点之间通过指针连接，支持范围查询");
        System.out.println("4. 树的高度保持平衡，所有叶子节点都在同一层");
        System.out.println();
        
        // 演示范围查询（通过叶子节点链表）
        System.out.println("--- 范围查询演示 ---");
        System.out.println("通过叶子节点的链表结构，可以高效地进行范围查询");
        BPlusTreeNode leaf = tree.getRoot();
        while (!leaf.isLeaf) {
            leaf = leaf.getChild(0);
        }
        
        System.out.print("叶子节点链表中的数据: ");
        while (leaf != null) {
            System.out.print(leaf.keys + " -> ");
            leaf = leaf.nextLeaf;
        }
        System.out.println("null");
        
        // ========================================
        // 演示删除操作
        // ========================================
        System.out.println("\n\n=== 删除操作演示 ===\n");
        
        // 演示1：删除叶子节点中的键（不引发下溢）
        System.out.println("--- 删除操作1：删除不引发下溢的键 ---");
        System.out.println("当前树的结构:");
        tree.printTree();
        System.out.println("所有键: " + tree.getAllKeys());
        System.out.println();
        
        int deleteKey1 = 7;
        System.out.println("删除键: " + deleteKey1);
        boolean success1 = tree.delete(deleteKey1);
        System.out.println("删除结果: " + (success1 ? "成功" : "失败（键不存在）"));
        System.out.println("树的高度: " + tree.getHeight());
        System.out.println("树的结构:");
        tree.printTree();
        System.out.println("所有键: " + tree.getAllKeys());
        System.out.println("是否有序: " + isSorted(tree.getAllKeys()));
        System.out.println();
        
        // 演示2：删除引发借位操作的键
        System.out.println("--- 删除操作2：删除引发借位操作 ---");
        int deleteKey2 = 5;
        System.out.println("删除键: " + deleteKey2);
        System.out.println("这将导致叶子节点下溢，需要从兄弟节点借位");
        boolean success2 = tree.delete(deleteKey2);
        System.out.println("删除结果: " + (success2 ? "成功" : "失败（键不存在）"));
        System.out.println("树的高度: " + tree.getHeight());
        System.out.println("树的结构:");
        tree.printTree();
        System.out.println("所有键: " + tree.getAllKeys());
        System.out.println("是否有序: " + isSorted(tree.getAllKeys()));
        System.out.println();
        
        // 演示3：删除引发合并操作的键
        System.out.println("--- 删除操作3：删除引发合并操作 ---");
        int deleteKey3 = 10;
        System.out.println("删除键: " + deleteKey3);
        System.out.println("这将导致节点下溢且无法借位，需要合并节点");
        boolean success3 = tree.delete(deleteKey3);
        System.out.println("删除结果: " + (success3 ? "成功" : "失败（键不存在）"));
        System.out.println("树的高度: " + tree.getHeight());
        System.out.println("树的结构:");
        tree.printTree();
        System.out.println("所有键: " + tree.getAllKeys());
        System.out.println("是否有序: " + isSorted(tree.getAllKeys()));
        System.out.println();
        
        // 演示4：删除不存在的键
        System.out.println("--- 删除操作4：删除不存在的键 ---");
        int deleteKey4 = 99;
        System.out.println("删除键: " + deleteKey4);
        boolean success4 = tree.delete(deleteKey4);
        System.out.println("删除结果: " + (success4 ? "成功" : "失败（键不存在）"));
        System.out.println();
        
        // 演示5：连续删除，观察树的变化
        System.out.println("--- 删除操作5：连续删除多个键 ---");
        int[] deleteKeys = {12, 17, 20};
        for (int key : deleteKeys) {
            System.out.println("删除键: " + key);
            tree.delete(key);
            System.out.println("树的高度: " + tree.getHeight());
            System.out.println("树的结构:");
            tree.printTree();
            System.out.println("所有键: " + tree.getAllKeys());
            System.out.println();
        }
        
        // 演示6：删除所有键，树变为空树
        System.out.println("--- 删除操作6：删除最后一个键 ---");
        System.out.println("删除键: 30");
        tree.delete(30);
        System.out.println("树的高度: " + tree.getHeight());
        System.out.println("树的结构:");
        tree.printTree();
        System.out.println("所有键: " + tree.getAllKeys());
        System.out.println("树是否为空: " + tree.getRoot().isEmpty());
    }
    
    /**
     * 检查列表是否有序
     * 
     * @param list 要检查的列表
     * @return 如果有序返回true
     */
    private static boolean isSorted(List<Integer> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) < list.get(i - 1)) {
                return false;
            }
        }
        return true;
    }
}