package com.test.bplustree;

import java.util.ArrayList;
import java.util.List;

/**
 * B+树实现类
 * <br/>
 * B+树是一种自平衡的多路搜索树，常用于数据库和文件系统的索引结构
 * 主要特点：
 * 1. 所有数据都存储在叶子节点中
 * 2. 非叶子节点只起到索引作用，不存储实际数据
 * 3. 叶子节点之间通过指针连接，支持高效的范围查询
 * 4. 树的高度保持平衡，所有叶子节点都在同一层
 *
 * @author B+Tree Demo
 */
public class BPlusTree {

    /**
     * 根节点
     */
    private BPlusTreeNode root;

    /**
     * B+树的阶数（每个节点最多可以有的子节点数）
     * 例如：order=3表示每个节点最多有3个子节点，最多存储2个键
     */
    private final int order;

    /**
     * 构造函数
     *
     * @param order B+树的阶数
     */
    public BPlusTree(int order) {
        if (order < 3) {
            throw new IllegalArgumentException("阶数必须大于等于3");
        }
        this.order = order;
        // 初始时树为空，创建一个空的叶子节点作为根
        this.root = new BPlusTreeNode(true);
    }

    /**
     * 查找指定键的值
     * B+树的查找操作：从根节点开始，逐层向下查找，直到叶子节点
     *
     * @param key 要查找的键
     * @return 如果找到返回true，否则返回false
     */
    public boolean search(int key) {
        // 如果树为空，直接返回false
        if (root == null) {
            return false;
        }

        // 从根节点开始查找
        BPlusTreeNode current = findLeafNode(key);

        // 在叶子节点中查找key
        for (int i = 0; i < current.getKeyCount(); i++) {
            if (current.getKey(i) == key) {
                return true; // 找到了
            }
        }

        return false; // 未找到
    }

    /**
     * 插入键值
     * B+树的插入操作总是在叶子节点中进行
     * 如果节点满了，需要进行分裂操作
     *
     * @param key 要插入的键
     */
    public void insert(int key) {
        // 如果树为空，直接插入到根节点
        if (root.isEmpty()) {
            root.keys.add(key);
            return;
        }

        // 找到应该插入的叶子节点
        BPlusTreeNode leaf = findLeafNode(key);

        // 在叶子节点中插入键
        insertIntoLeaf(leaf, key);

        // 如果叶子节点溢出，进行分裂
        if (leaf.getKeyCount() > order - 1) {
            splitLeaf(leaf);
        }
    }

    /**
     * 找到应该插入指定键的叶子节点
     *
     * @param key 键值
     * @return 应该插入的叶子节点
     */
    private BPlusTreeNode findLeafNode(int key) {
        BPlusTreeNode current = root;

        // 逐层向下查找，直到叶子节点
        while (!current.isLeaf) {
            int i = 0;
            // 找到第一个大于等于key的位置
            while (i < current.getKeyCount() && key >= current.getKey(i)) {
                i++;
            }
            // 移动到对应的子节点
            current = current.getChild(i);
        }

        return current;
    }

    /**
     * 在叶子节点中插入键
     * 保持叶子节点中的键有序
     *
     * @param leaf 叶子节点
     * @param key  要插入的键
     */
    private void insertIntoLeaf(BPlusTreeNode leaf, int key) {
        int i = 0;
        // 找到插入位置，保持有序
        while (i < leaf.getKeyCount() && leaf.getKey(i) < key) {
            i++;
        }
        leaf.insertKey(i, key);
    }

    /**
     * 分裂叶子节点
     * 当叶子节点中的键数量超过阶数-1时，需要进行分裂
     *
     * @param leaf 要分裂的叶子节点
     */
    private void splitLeaf(BPlusTreeNode leaf) {
        // 计算中间位置
        int mid = leaf.getKeyCount() / 2;

        // 创建新的叶子节点
        BPlusTreeNode newLeaf = new BPlusTreeNode(true);

        // 将后半部分的键移到新节点
        for (int i = leaf.getKeyCount() - 1; i >= mid; i--) {
            newLeaf.keys.addFirst(leaf.removeKey(i));
        }

        // 维护叶子节点之间的链接
        newLeaf.nextLeaf = leaf.nextLeaf;
        leaf.nextLeaf = newLeaf;

        // 如果分裂的是根节点，需要创建新的根
        if (leaf == root) {
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.keys.add(newLeaf.getKey(0));
            newRoot.children.add(leaf);
            newRoot.children.add(newLeaf);
            leaf.parent = newRoot;
            newLeaf.parent = newRoot;
            root = newRoot;
        } else {
            // 将新节点的最小键值提升到父节点
            insertIntoParent(leaf, newLeaf.getKey(0), newLeaf);
        }
    }

    /**
     * 将键插入到父节点中
     * 用于处理节点分裂后的晋升操作
     *
     * @param leftNode  左子节点
     * @param key       要插入的键
     * @param rightNode 右子节点
     */
    private void insertIntoParent(BPlusTreeNode leftNode, int key, BPlusTreeNode rightNode) {
        BPlusTreeNode parent = leftNode.parent;

        // 找到leftNode在父节点中的位置
        int index = 0;
        while (index < parent.children.size() && parent.getChild(index) != leftNode) {
            index++;
        }

        // 在父节点中插入键和右子节点
        parent.insertKey(index, key);
        parent.insertChild(index + 1, rightNode);
        rightNode.parent = parent;

        // 如果父节点溢出，继续分裂
        if (parent.getKeyCount() > order - 1) {
            splitInternal(parent);
        }
    }

    /**
     * 分裂内部节点（非叶子节点）
     *
     * @param node 要分裂的内部节点
     */
    private void splitInternal(BPlusTreeNode node) {
        // 计算中间位置
        int mid = node.getKeyCount() / 2;

        // 创建新的内部节点
        BPlusTreeNode newNode = new BPlusTreeNode(false);

        // 将中间位置的键提升到父节点
        int midKey = node.getKey(mid);

        // 将后半部分的键和子节点移到新节点
        for (int i = node.getKeyCount() - 1; i > mid; i--) {
            newNode.keys.addFirst(node.removeKey(i));
        }
        // 移除中间位置的键（这个键将被提升到父节点）
        node.removeKey(mid);

        // 移动子节点
        for (int i = node.children.size() - 1; i > mid; i--) {
            newNode.children.addFirst(node.removeChild(i));
        }

        // 更新子节点的父节点引用
        for (BPlusTreeNode child : newNode.children) {
            child.parent = newNode;
        }

        // 如果分裂的是根节点，需要创建新的根
        if (node == root) {
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.keys.add(midKey);
            newRoot.children.add(node);
            newRoot.children.add(newNode);
            node.parent = newRoot;
            newNode.parent = newRoot;
            root = newRoot;
        } else {
            // 将中间键提升到父节点
            insertIntoParent(node, midKey, newNode);
        }

    }

    /**
     * 删除指定键
     * B+树的删除操作比插入更复杂，需要处理下溢、借位和合并等情况
     *
     * @param key 要删除的键
     * @return 如果删除成功返回true，键不存在返回false
     */
    public boolean delete(int key) {
        // 如果树为空，直接返回
        if (root.isEmpty()) {
            return false;
        }

        // 找到包含该键的叶子节点
        BPlusTreeNode leaf = findLeafNode(key);

        // 在叶子节点中查找并删除键
        int keyIndex = -1;
        for (int i = 0; i < leaf.getKeyCount(); i++) {
            if (leaf.getKey(i) == key) {
                keyIndex = i;
                break;
            }
        }

        // 如果键不存在，返回false
        if (keyIndex == -1) {
            return false;
        }

        // 删除键
        leaf.removeKey(keyIndex);

        // 如果删除的是根节点且为空，树变为空树
        if (leaf == root && leaf.isEmpty()) {
            return true;
        }

        // 检查是否需要处理下溢（键数太少）
        int minKeys = getMinKeys();
        if (leaf.getKeyCount() < minKeys && leaf != root) {
            handleUnderflow(leaf);
        } else if (leaf == root && leaf.isEmpty()) {
            // 根节点为空且不是唯一的节点，需要收缩树的高度
            if (!root.isLeaf && root.getKeyCount() == 0) {
                root = root.getChild(0);
                root.parent = null;
            }
        }

        return true;
    }

    /**
     * 获取节点最少需要的键数
     * 对于阶数为order的B+树，非根节点最少需要 ⌈order/2⌉ - 1 个键
     *
     * @return 最少键数
     */
    private int getMinKeys() {
        return (order + 1) / 2 - 1;
    }

    /**
     * 处理节点下溢（键数太少）
     * 策略：
     * 1. 尝试从左兄弟借键
     * 2. 尝试从右兄弟借键
     * 3. 如果都不能借，则合并节点
     *
     * @param node 下溢的节点
     */
    private void handleUnderflow(BPlusTreeNode node) {
        BPlusTreeNode parent = node.parent;

        // 找到node在父节点的子节点列表中的位置
        int index = 0;
        while (index < parent.children.size() && parent.getChild(index) != node) {
            index++;
        }

        // 尝试从左兄弟借键（如果存在）
        if (index > 0) {
            BPlusTreeNode leftSibling = parent.getChild(index - 1);
            if (leftSibling.getKeyCount() > getMinKeys()) {
                borrowFromLeft(node, leftSibling, index - 1);
                return;
            }
        }

        // 尝试从右兄弟借键（如果存在）
        if (index < parent.children.size() - 1) {
            BPlusTreeNode rightSibling = parent.getChild(index + 1);
            if (rightSibling.getKeyCount() > getMinKeys()) {
                borrowFromRight(node, rightSibling, index);
                return;
            }
        }

        // 无法借键，执行合并操作
        if (index > 0) {
            // 与左兄弟合并
            BPlusTreeNode leftSibling = parent.getChild(index - 1);
            merge(leftSibling, node, index - 1);
        } else {
            // 与右兄弟合并
            BPlusTreeNode rightSibling = parent.getChild(index + 1);
            merge(node, rightSibling, index);
        }
    }

    /**
     * 从左兄弟节点借键
     *
     * @param node        下溢的节点
     * @param leftSibling 左兄弟节点
     * @param parentIndex 左兄弟在父节点中的索引
     */
    private void borrowFromLeft(BPlusTreeNode node, BPlusTreeNode leftSibling, int parentIndex) {
        BPlusTreeNode parent = node.parent;

        if (node.isLeaf) {
            // 叶子节点：从左兄弟借最大的键，插入到当前节点的最前面
            int borrowedKey = leftSibling.removeKey(leftSibling.getKeyCount() - 1);
            node.insertKey(0, borrowedKey);

            // 更新父节点中的分隔键（使用node的第一个键）
            parent.setKey(parentIndex, node.getKey(0));
        } else {
            // 内部节点：需要借键和子节点
            // 1. 从父节点降下一个键到当前节点
            int parentKey = parent.getKey(parentIndex);
            node.insertKey(0, parentKey);

            // 2. 从左兄弟借最大的键上升到父节点
            int borrowedKey = leftSibling.removeKey(leftSibling.getKeyCount() - 1);
            parent.setKey(parentIndex, borrowedKey);

            // 3. 从左兄弟借最右边的子节点
            BPlusTreeNode borrowedChild = leftSibling.removeChild(leftSibling.children.size() - 1);
            node.insertChild(0, borrowedChild);
            borrowedChild.parent = node;
        }
    }

    /**
     * 从右兄弟节点借键
     *
     * @param node         下溢的节点
     * @param rightSibling 右兄弟节点
     * @param parentIndex  node在父节点中的索引
     */
    private void borrowFromRight(BPlusTreeNode node, BPlusTreeNode rightSibling, int parentIndex) {
        BPlusTreeNode parent = node.parent;

        if (node.isLeaf) {
            // 叶子节点：从右兄弟借最小的键，追加到当前节点的最后面
            int borrowedKey = rightSibling.removeKey(0);
            node.keys.add(borrowedKey);

            // 更新父节点中的分隔键（使用右兄弟的第一个键）
            parent.setKey(parentIndex, rightSibling.getKey(0));
        } else {
            // 内部节点：需要借键和子节点
            // 1. 从父节点降下一个键到当前节点（放在最后）
            int parentKey = parent.getKey(parentIndex);
            node.keys.add(parentKey);

            // 2. 从右兄弟借最小的键上升到父节点
            int borrowedKey = rightSibling.removeKey(0);
            parent.setKey(parentIndex, borrowedKey);

            // 3. 从右兄弟借最左边的子节点，追加到当前节点最后
            BPlusTreeNode borrowedChild = rightSibling.removeChild(0);
            node.children.add(borrowedChild);
            borrowedChild.parent = node;
        }
    }

    /**
     * 合并两个节点
     * 将rightNode合并到leftNode中，并从父节点删除分隔键
     *
     * @param leftNode    左节点
     * @param rightNode   右节点
     * @param parentIndex leftNode在父节点中的索引（也是分隔键的索引）
     */
    private void merge(BPlusTreeNode leftNode, BPlusTreeNode rightNode, int parentIndex) {
        BPlusTreeNode parent = leftNode.parent;

        if (leftNode.isLeaf) {
            // 叶子节点合并：将rightNode的所有键追加到leftNode
            leftNode.keys.addAll(rightNode.keys);

            // 维护叶子节点链表：跳过rightNode
            leftNode.nextLeaf = rightNode.nextLeaf;

            // 从父节点中移除分隔键和rightNode的引用
            parent.removeKey(parentIndex);
        } else {
            // 内部节点合并：
            // 1. 从父节点降下分隔键到leftNode的最后
            int parentKey = parent.removeKey(parentIndex);
            leftNode.keys.add(parentKey);

            // 2. 将rightNode的所有键和子节点追加到leftNode
            leftNode.keys.addAll(rightNode.keys);
            leftNode.children.addAll(rightNode.children);

            // 3. 更新子节点的父引用
            for (BPlusTreeNode child : rightNode.children) {
                child.parent = leftNode;
            }
        }

        // 从父节点中移除rightNode的引用
        parent.removeChild(parentIndex + 1);

        // 检查父节点是否下溢
        int minKeys = getMinKeys();
        if (parent.getKeyCount() < minKeys && parent != root) {
            handleUnderflow(parent);
        } else if (parent == root && parent.isEmpty()) {
            // 根节点为空，树的高度减1
            if (!parent.isLeaf && !parent.children.isEmpty()) {
                root = parent.getChild(0);
                root.parent = null;
            }
        }
    }

    /**
     * 获取树的高度
     *
     * @return 树的高度
     */
    public int getHeight() {
        if (root == null) {
            return 0;
        }

        int height = 1;
        BPlusTreeNode current = root;

        // 从根节点一直向下到叶子节点
        while (!current.isLeaf) {
            height++;
            current = current.getChild(0);
        }

        return height;
    }

    /**
     * 获取所有叶子节点中的键（按顺序）
     * 用于验证B+树的正确性
     *
     * @return 有序的键列表
     */
    public List<Integer> getAllKeys() {
        List<Integer> keys = new ArrayList<>();

        // 找到最左边的叶子节点
        BPlusTreeNode current = root;
        while (!current.isLeaf) {
            current = current.getChild(0);
        }

        // 遍历所有叶子节点
        while (current != null) {
            keys.addAll(current.keys);
            current = current.nextLeaf;
        }

        return keys;
    }

    /**
     * 打印B+树结构（简化版）
     */
    public void printTree() {
        printTree(root, 0);
    }

    /**
     * 递归打印树结构
     *
     * @param node  当前节点
     * @param depth 当前深度
     */
    private void printTree(BPlusTreeNode node, int depth) {
        if (node == null) {
            return;
        }

        // 打印缩进
        String indent = "  ".repeat(depth);

        if (node.isLeaf) {
            System.out.println(indent + "叶子节点: " + node.keys);
        } else {
            System.out.println(indent + "内部节点: " + node.keys);
            // 打印子节点
            for (BPlusTreeNode child : node.children) {
                printTree(child, depth + 1);
            }
        }
    }

    /**
     * 获取阶数
     *
     * @return 阶数
     */
    public int getOrder() {
        return order;
    }

    /**
     * 获取根节点
     *
     * @return 根节点
     */
    public BPlusTreeNode getRoot() {
        return root;
    }
}
