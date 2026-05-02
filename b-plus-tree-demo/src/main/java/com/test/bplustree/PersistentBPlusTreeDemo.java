package com.test.bplustree;

import java.util.List;

/**
 * 持久化B+树演示程序
 * 演示基于数据页的持久化存储功能
 * 
 * @author B+Tree Demo
 */
public class PersistentBPlusTreeDemo {
    
    public static void main(String[] args) {
        String dbFile = "bplus_tree.db";
        
        try {
            System.out.println("=== 持久化B+树演示 ===\n");
            
            // ========================================
            // 第一阶段：创建数据库并插入数据
            // ========================================
            System.out.println("--- 第一阶段：创建数据库并插入数据 ---\n");
            
            PersistentBPlusTree tree = new PersistentBPlusTree(3);
            tree.open(dbFile);
            System.out.println("数据库已创建: " + dbFile);
            System.out.println("B+树阶数: " + tree.getOrder());
            System.out.println();
            
            // 插入数据
            System.out.println("插入数据:");
            tree.insert(10, "张三");
            System.out.println("  插入: 10 -> 张三");
            
            tree.insert(20, "李四");
            System.out.println("  插入: 20 -> 李四");
            
            tree.insert(5, "王五");
            System.out.println("  插入: 5 -> 王五");
            
            tree.insert(15, "赵六");
            System.out.println("  插入: 15 -> 赵六");
            
            tree.insert(25, "钱七");
            System.out.println("  插入: 25 -> 钱七");
            
            System.out.println();
            
            // 打印树结构
            System.out.println("当前树结构:");
            tree.printTree();
            System.out.println();
            
            // ========================================
            // 第二阶段：查询数据
            // ========================================
            System.out.println("--- 第二阶段：查询数据 ---\n");
            
            // 精确查询
            System.out.println("精确查询:");
            TreeRecord record1 = tree.search(10);
            System.out.println("  查找键 10: " + (record1 != null ? record1.value() : "未找到"));
            
            TreeRecord record2 = tree.search(20);
            System.out.println("  查找键 20: " + (record2 != null ? record2.value() : "未找到"));
            
            TreeRecord record3 = tree.search(99);
            System.out.println("  查找键 99: " + (record3 != null ? record3.value() : "未找到"));
            System.out.println();
            
            // 范围查询
            System.out.println("范围查询 [10, 25]:");
            List<TreeRecord> rangeResults = tree.rangeQuery(10, 25);
            for (TreeRecord r : rangeResults) {
                System.out.println("  " + r.key() + " -> " + r.value());
            }
            System.out.println();
            
            // ========================================
            // 第三阶段：关闭数据库（数据持久化到磁盘）
            // ========================================
            System.out.println("--- 第三阶段：关闭数据库（数据持久化） ---\n");
            
            tree.close();
            System.out.println("数据库已关闭，所有数据已保存到磁盘");
            System.out.println();
            
            // ========================================
            // 第四阶段：重新打开数据库，验证持久化
            // ========================================
            System.out.println("--- 第四阶段：重新打开数据库（验证持久化） ---\n");
            
            PersistentBPlusTree tree2 = new PersistentBPlusTree(3);
            tree2.open(dbFile);
            System.out.println("数据库已重新打开");
            System.out.println();
            
            // 验证数据是否存在
            System.out.println("验证持久化的数据:");
            TreeRecord persistedRecord1 = tree2.search(10);
            System.out.println("  查找键 10: " + (persistedRecord1 != null ? persistedRecord1.value() : "未找到"));
            
            TreeRecord persistedRecord2 = tree2.search(15);
            System.out.println("  查找键 15: " + (persistedRecord2 != null ? persistedRecord2.value() : "未找到"));
            
            TreeRecord persistedRecord3 = tree2.search(25);
            System.out.println("  查找键 25: " + (persistedRecord3 != null ? persistedRecord3.value() : "未找到"));
            System.out.println();
            
            // 打印树结构（应该与关闭前相同）
            System.out.println("重启后的树结构:");
            tree2.printTree();
            System.out.println();
            
            // ========================================
            // 第五阶段：继续插入新数据
            // ========================================
            System.out.println("--- 第五阶段：继续插入新数据 ---\n");
            
            tree2.insert(30, "孙八");
            System.out.println("  插入: 30 -> 孙八");
            
            tree2.insert(8, "周九");
            System.out.println("  插入: 8 -> 周九");
            
            System.out.println();
            System.out.println("更新后的树结构:");
            tree2.printTree();
            System.out.println();
            
            // ========================================
            // 第六阶段：删除数据
            // ========================================
            System.out.println("--- 第六阶段：删除数据 ---\n");
            
            boolean deleted = tree2.delete(15);
            System.out.println("  删除键 15: " + (deleted ? "成功" : "失败"));
            
            deleted = tree2.delete(20);
            System.out.println("  删除键 20: " + (deleted ? "成功" : "失败"));
            
            System.out.println();
            System.out.println("删除后的树结构:");
            tree2.printTree();
            System.out.println();
            
            // ========================================
            // 第七阶段：最终查询验证
            // ========================================
            System.out.println("--- 第七阶段：最终查询验证 ---\n");
            
            System.out.println("查询所有剩余数据:");
            List<TreeRecord> allRecords = tree2.rangeQuery(0, 100);
            for (TreeRecord r : allRecords) {
                System.out.println("  " + r.key() + " -> " + r.value());
            }
            System.out.println();
            
            // 关闭数据库
            tree2.close();
            System.out.println("数据库已关闭");
            System.out.println();
            
            System.out.println("=== 演示完成 ===");
            System.out.println("数据文件: " + dbFile);
            System.out.println("您可以重新运行程序来验证数据持久化！");
            
        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
