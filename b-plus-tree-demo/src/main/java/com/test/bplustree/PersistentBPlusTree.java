package com.test.bplustree;

import java.util.ArrayList;
import java.util.List;

/**
 * 持久化B+树：支持基于数据页的持久化存储
 * 模拟MySQL InnoDB存储引擎的页管理机制
 *
 */
public class PersistentBPlusTree {
    /**
     * B+树的阶数
     */
    private final int order;

    /**
     * 页管理器
     */
    private PageManager pageManager;

    /**
     * 页序列化器
     */
    private final PageSerializer pageSerializer;

    /**
     * 缓冲池
     */
    private final BufferPool bufferPool;

    /**
     * 根节点页号
     */
    private int rootPageNum;

    /**
     * 指定阶数的构造函数
     *
     * @param order B+树的阶数
     */
    public PersistentBPlusTree(int order) {
        if (order < 3) {
            throw new IllegalArgumentException("阶数必须大于等于3");
        }
        this.order = order;
        this.pageSerializer = new PageSerializer();
        // 缓存100个页
        this.bufferPool = new BufferPool(100);
    }

    /**
     * 打开数据库
     *
     * @param filePath 数据库文件路径
     * @throws Exception 如果打开失败
     */
    public void open(String filePath) throws Exception {
        this.pageManager = new PageManager(filePath);
        this.pageManager.open();

        // 检查是否是新的数据库文件
        if (pageManager.getTotalPages() == 0) {
            // 新建数据库：初始化文件头和根节点
            pageManager.initHeader();
            int newPageNum = pageManager.allocatePage();

            // 创建空的根节点（叶子节点）
            BPlusTreeNode rootNode = new BPlusTreeNode(true);
            rootNode.setPageNum(newPageNum);

            // 序列化并写入磁盘
            byte[] pageData = pageSerializer.serializeLeafNode(
                    rootNode.getRecords(),
                    newPageNum,
                    -1,  // 父节点页号
                    -1,  // 下一个叶子页号
                    -1   // 前一个叶子页号
            );
            pageManager.writePage(newPageNum, pageData);

            rootPageNum = newPageNum;
        } else {
            // 打开已有数据库：读取根节点页号
            rootPageNum = pageManager.readRootPageNum();
        }
    }

    /**
     * 关闭数据库
     * 刷新所有脏页到磁盘
     *
     * @throws Exception 如果关闭失败
     */
    public void close() throws Exception {
        if (pageManager != null) {
            // 刷新所有脏页
            bufferPool.flushAll(pageManager);

            // 关闭文件
            pageManager.close();
        }
    }

    /**
     * 插入记录
     *
     * @param key   键
     * @param value 值
     * @throws Exception 如果插入失败
     */
    public void insert(int key, String value) throws Exception {
        Record record = new Record(key, value);

        // 找到应该插入的叶子节点
        BPlusTreeNode leaf = findLeafNode(rootPageNum, key);

        // 在叶子节点中插入记录
        insertRecordIntoLeaf(leaf, record);

        // 序列化并标记为脏页
        saveNode(leaf);

        // 检查是否需要分裂
        if (leaf.getRecords().size() > order - 1) {
            splitLeaf(leaf);
        }
    }

    /**
     * 查找记录
     *
     * @param key 键
     * @return 记录，如果不存在返回null
     * @throws Exception 如果查找失败
     */
    public Record search(int key) throws Exception {
        // 找到包含该键的叶子节点
        BPlusTreeNode leaf = findLeafNode(rootPageNum, key);

        // 在叶子节点中查找记录
        for (Record record : leaf.getRecords()) {
            if (record.getKey() == key) {
                return record;
            }
        }

        return null;
    }

    /**
     * 删除记录
     *
     * @param key 键
     * @return 如果删除成功返回true
     * @throws Exception 如果删除失败
     */
    public boolean delete(int key) throws Exception {
        // 找到包含该键的叶子节点
        BPlusTreeNode leaf = findLeafNode(rootPageNum, key);

        // 在叶子节点中查找并删除记录
        for (int i = 0; i < leaf.getRecords().size(); i++) {
            if (leaf.getRecords().get(i).getKey() == key) {
                leaf.removeRecord(i);

                // 保存修改
                saveNode(leaf);

                // 检查下溢
                int minKeys = getMinKeys();
                if (leaf.getRecords().size() < minKeys && leaf.getPageNum() != rootPageNum) {
                    handleUnderflow(leaf);
                } else if (!leaf.getRecords().isEmpty()) {
                    refreshAncestorSeparators(leaf);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * 范围查询
     *
     * @param startKey 起始键
     * @param endKey   结束键
     * @return 范围内的记录列表
     * @throws Exception 如果查询失败
     */
    public List<Record> rangeQuery(int startKey, int endKey) throws Exception {
        List<Record> results = new ArrayList<>();

        // 找到起始键所在的叶子节点
        BPlusTreeNode leaf = findLeafNode(rootPageNum, startKey);

        // 遍历叶子节点链表
        while (leaf != null && leaf.getPageNum() != -1) {
            for (Record record : leaf.getRecords()) {
                if (record.getKey() >= startKey && record.getKey() <= endKey) {
                    results.add(record);
                }
                if (record.getKey() > endKey) {
                    return results;
                }
            }

            // 移动到下一个叶子节点
            int nextPageNum = leaf.getNextLeafPageNum();
            if (nextPageNum == -1) {
                break;
            }

            // 读取下一个叶子节点
            byte[] pageData = bufferPool.getPage(nextPageNum, pageManager);
            leaf = pageSerializer.deserializeNode(pageData, nextPageNum);
        }

        return results;
    }

    /**
     * 强制刷新到磁盘
     *
     * @throws Exception 如果刷新失败
     */
    public void flush() throws Exception {
        if (pageManager != null) {
            bufferPool.flushAll(pageManager);
        }
    }

    /**
     * 找到包含指定键的叶子节点
     *
     * @param pageNum 当前节点页号
     * @param key     键
     * @return 叶子节点
     * @throws Exception 如果查找失败
     */
    private BPlusTreeNode findLeafNode(int pageNum, int key) throws Exception {
        // 从缓冲池或磁盘读取节点
        byte[] pageData = bufferPool.getPage(pageNum, pageManager);
        BPlusTreeNode node = pageSerializer.deserializeNode(pageData, pageNum);

        // 如果是叶子节点，直接返回
        if (node.isLeaf) {
            return node;
        }

        // 如果是内部节点，找到合适的子节点
        int i = 0;
        while (i < node.getKeyCount() && key >= node.getKey(i)) {
            i++;
        }

        // 递归查找子节点
        int childPageNum = node.getChildPageNum(i);
        return findLeafNode(childPageNum, key);
    }

    /**
     * 在叶子节点中插入记录
     *
     * @param leaf   叶子节点
     * @param record 记录
     */
    private void insertRecordIntoLeaf(BPlusTreeNode leaf, Record record) {
        int i = 0;
        // 找到插入位置，保持有序
        while (i < leaf.getRecords().size() && leaf.getRecords().get(i).getKey() < record.getKey()) {
            i++;
        }
        leaf.insertRecord(i, record);
    }

    /**
     * 保存节点到缓冲池（标记为脏页）
     *
     * @param node 节点
     * @throws Exception 如果保存失败
     */
    private void saveNode(BPlusTreeNode node) throws Exception {
        byte[] pageData;

        if (node.isLeaf) {
            pageData = pageSerializer.serializeLeafNode(
                    node.getRecords(),
                    node.getPageNum(),
                    node.getParentPageNum(),
                    node.getNextLeafPageNum(),
                    node.getPrevLeafPageNum()
            );
        } else {
            pageData = pageSerializer.serializeInternalNode(
                    node.keys,
                    node.getChildPageNums(),
                    node.getPageNum(),
                    node.getParentPageNum()
            );
        }

        // 标记为脏页
        bufferPool.markDirty(node.getPageNum(), pageData);
    }

    /**
     * 分裂叶子节点
     *
     * @param leaf 要分裂的叶子节点
     * @throws Exception 如果分裂失败
     */
    private void splitLeaf(BPlusTreeNode leaf) throws Exception {
        int mid = leaf.getRecords().size() / 2;

        // 创建新的叶子节点
        int newPageNum = pageManager.allocatePage();
        BPlusTreeNode newLeaf = new BPlusTreeNode(true);
        newLeaf.setPageNum(newPageNum);
        newLeaf.setParentPageNum(leaf.getParentPageNum());

        // 移动后半部分的记录到新节点
        for (int i = leaf.getRecords().size() - 1; i >= mid; i--) {
            Record record = leaf.removeRecord(i);
            newLeaf.insertRecord(0, record);
        }

        // 维护叶子节点链表
        newLeaf.setNextLeafPageNum(leaf.getNextLeafPageNum());
        newLeaf.setPrevLeafPageNum(leaf.getPageNum());
        leaf.setNextLeafPageNum(newPageNum);
        if (newLeaf.getNextLeafPageNum() != -1) {
            byte[] nextLeafData = bufferPool.getPage(newLeaf.getNextLeafPageNum(), pageManager);
            BPlusTreeNode nextLeaf = pageSerializer.deserializeNode(nextLeafData, newLeaf.getNextLeafPageNum());
            nextLeaf.setPrevLeafPageNum(newPageNum);
            saveNode(nextLeaf);
        }

        // 如果分裂的是根节点，需要创建新的根
        if (leaf.getPageNum() == rootPageNum) {
            int newRootPageNum = pageManager.allocatePage();
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.setPageNum(newRootPageNum);
            newRoot.addKey(newLeaf.getRecords().getFirst().getKey());
            newRoot.addChildPageNum(leaf.getPageNum());
            newRoot.addChildPageNum(newPageNum);

            // 设置子节点的父节点页号
            leaf.setParentPageNum(newRootPageNum);
            newLeaf.setParentPageNum(newRootPageNum);

            // 保存所有节点
            saveNode(leaf);
            saveNode(newLeaf);
            saveNode(newRoot);

            rootPageNum = newRootPageNum;
            pageManager.updateRootPageNum(rootPageNum);
        } else {
            // 将新节点的最小键值提升到父节点
            // 注意：需要先保存修改后的 leaf 节点
            saveNode(leaf);
            saveNode(newLeaf);
            insertIntoParent(leaf, newLeaf.getRecords().getFirst().getKey(), newPageNum);
        }
    }

    /**
     * 将键插入到父节点中
     *
     * @param leftNode     左子节点
     * @param key          要插入的键
     * @param rightPageNum 右子节点页号
     * @throws Exception 如果插入失败
     */
    private void insertIntoParent(BPlusTreeNode leftNode, int key, int rightPageNum) throws Exception {
        int parentPageNum = leftNode.getParentPageNum();

        // 读取父节点
        byte[] pageData = bufferPool.getPage(parentPageNum, pageManager);
        BPlusTreeNode parent = pageSerializer.deserializeNode(pageData, parentPageNum);

        // 找到leftNode在父节点中的位置
        int index = 0;
        while (index < parent.getChildPageNums().size() && parent.getChildPageNum(index) != leftNode.getPageNum()) {
            index++;
        }
        if (index == parent.getChildPageNums().size()) {
            throw new IllegalStateException(
                    "父节点 " + parentPageNum + " 中找不到子节点 " + leftNode.getPageNum() +
                            "，当前子页列表: " + parent.getChildPageNums()
            );
        }

        // 在父节点中插入键和右子节点页号
        parent.insertKey(index, key);
        parent.insertChildPageNum(index + 1, rightPageNum);

        // 保存父节点
        saveNode(parent);

        // 如果父节点溢出，继续分裂
        if (parent.getKeyCount() > order - 1) {
            splitInternal(parent);
        }
    }

    /**
     * 分裂内部节点
     *
     * @param node 要分裂的内部节点
     * @throws Exception 如果分裂失败
     */
    private void splitInternal(BPlusTreeNode node) throws Exception {
        int mid = node.getKeyCount() / 2;
        int midKey = node.getKey(mid);

        // 创建新的内部节点
        int newPageNum = pageManager.allocatePage();
        BPlusTreeNode newNode = new BPlusTreeNode(false);
        newNode.setPageNum(newPageNum);
        newNode.setParentPageNum(node.getParentPageNum());

        // 移动后半部分的键和子节点到新节点
        for (int i = node.getKeyCount() - 1; i > mid; i--) {
            int key = node.removeKey(i);
            newNode.insertKey(0, key);
        }
        node.removeKey(mid);  // 移除中间位置的键

        for (int i = node.getChildPageNums().size() - 1; i > mid; i--) {
            int childPageNum = node.removeChildPageNum(i);
            newNode.insertChildPageNum(0, childPageNum);
        }

        // 更新被移动到新内部节点下的子节点父页号
        for (int childPageNum : newNode.getChildPageNums()) {
            byte[] childPageData = bufferPool.getPage(childPageNum, pageManager);
            BPlusTreeNode childNode = pageSerializer.deserializeNode(childPageData, childPageNum);
            childNode.setParentPageNum(newPageNum);
            saveNode(childNode);
        }

        // 如果分裂的是根节点，需要创建新的根
        if (node.getPageNum() == rootPageNum) {
            int newRootPageNum = pageManager.allocatePage();
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.setPageNum(newRootPageNum);
            newRoot.addKey(midKey);
            newRoot.addChildPageNum(node.getPageNum());
            newRoot.addChildPageNum(newPageNum);

            node.setParentPageNum(newRootPageNum);
            newNode.setParentPageNum(newRootPageNum);

            saveNode(node);
            saveNode(newNode);
            saveNode(newRoot);

            rootPageNum = newRootPageNum;
            pageManager.updateRootPageNum(rootPageNum);
        } else {
            saveNode(node);
            saveNode(newNode);
            // 将中间键提升到父节点
            insertIntoParent(node, midKey, newPageNum);
        }
    }

    /**
     * 获取非根节点允许的最少键数
     *
     * @return 最少键数
     */
    private int getMinKeys() {
        return (order + 1) / 2 - 1;
    }

    /**
     * 从磁盘或缓冲池读取节点
     *
     * @param pageNum 页号
     * @return 节点
     * @throws Exception 如果读取失败
     */
    private BPlusTreeNode loadNode(int pageNum) throws Exception {
        byte[] pageData = bufferPool.getPage(pageNum, pageManager);
        return pageSerializer.deserializeNode(pageData, pageNum);
    }

    /**
     * 找到子节点在父节点中的位置
     *
     * @param parent       父节点
     * @param childPageNum 子节点页号
     * @return 索引位置
     */
    private int findChildIndex(BPlusTreeNode parent, int childPageNum) {
        int index = 0;
        while (index < parent.getChildPageNums().size() && parent.getChildPageNum(index) != childPageNum) {
            index++;
        }
        return index;
    }

    /**
     * 获取子树中的最小键值
     *
     * @param node 当前节点
     * @return 最小键值
     * @throws Exception 如果读取失败
     */
    private int getSubtreeMinKey(BPlusTreeNode node) throws Exception {
        BPlusTreeNode current = node;
        while (!current.isLeaf) {
            current = loadNode(current.getChildPageNum(0));
        }
        if (current.getRecords().isEmpty()) {
            throw new IllegalStateException("节点 " + current.getPageNum() + " 为空，无法获取最小键值");
        }
        return current.getRecords().getFirst().getKey();
    }

    /**
     * 自底向上刷新祖先分隔键
     *
     * @param node 起始节点
     * @throws Exception 如果更新失败
     */
    private void refreshAncestorSeparators(BPlusTreeNode node) throws Exception {
        BPlusTreeNode current = node;
        while (current.getParentPageNum() != -1) {
            BPlusTreeNode parent = loadNode(current.getParentPageNum());
            int childIndex = findChildIndex(parent, current.getPageNum());
            if (childIndex == parent.getChildPageNums().size()) {
                throw new IllegalStateException(
                        "父节点 " + parent.getPageNum() + " 中找不到子节点 " + current.getPageNum()
                );
            }
            if (childIndex > 0) {
                int newSeparator = getSubtreeMinKey(current);
                if (parent.getKey(childIndex - 1) != newSeparator) {
                    parent.setKey(childIndex - 1, newSeparator);
                    saveNode(parent);
                }
            }
            current = parent;
        }
    }

    /**
     * 根节点只剩一个子节点时收缩树高
     *
     * @param root 当前根节点
     * @throws Exception 如果更新失败
     */
    private void shrinkRootIfNeeded(BPlusTreeNode root) throws Exception {
        if (root.getPageNum() != rootPageNum || root.isLeaf || root.getKeyCount() != 0 ||
                root.getChildPageNums().isEmpty()) {
            return;
        }

        int newRootPageNum = root.getChildPageNum(0);
        BPlusTreeNode newRoot = loadNode(newRootPageNum);
        newRoot.setParentPageNum(-1);
        saveNode(newRoot);

        rootPageNum = newRootPageNum;
        pageManager.updateRootPageNum(rootPageNum);
    }

    /**
     * 从左兄弟借一个元素
     *
     * @param node        下溢节点
     * @param leftSibling 左兄弟
     * @param parent      父节点
     * @param parentIndex 父节点中的分隔键位置
     * @throws Exception 如果借位失败
     */
    private void borrowFromLeft(BPlusTreeNode node, BPlusTreeNode leftSibling, BPlusTreeNode parent,
                                int parentIndex) throws Exception {
        if (node.isLeaf) {
            Record borrowedRecord = leftSibling.removeRecord(leftSibling.getRecords().size() - 1);
            node.insertRecord(0, borrowedRecord);
            parent.setKey(parentIndex, node.getRecords().getFirst().getKey());
        } else {
            int parentKey = parent.getKey(parentIndex);
            int borrowedKey = leftSibling.removeKey(leftSibling.getKeyCount() - 1);
            int borrowedChildPageNum = leftSibling.removeChildPageNum(leftSibling.getChildPageNums().size() - 1);

            node.insertKey(0, parentKey);
            node.insertChildPageNum(0, borrowedChildPageNum);
            parent.setKey(parentIndex, borrowedKey);

            BPlusTreeNode borrowedChild = loadNode(borrowedChildPageNum);
            borrowedChild.setParentPageNum(node.getPageNum());
            saveNode(borrowedChild);
        }

        saveNode(leftSibling);
        saveNode(node);
        saveNode(parent);
    }

    /**
     * 从右兄弟借一个元素
     *
     * @param node         下溢节点
     * @param rightSibling 右兄弟
     * @param parent       父节点
     * @param parentIndex  父节点中的分隔键位置
     * @throws Exception 如果借位失败
     */
    private void borrowFromRight(BPlusTreeNode node, BPlusTreeNode rightSibling, BPlusTreeNode parent,
                                 int parentIndex) throws Exception {
        if (node.isLeaf) {
            Record borrowedRecord = rightSibling.removeRecord(0);
            node.addRecord(borrowedRecord);
            parent.setKey(parentIndex, rightSibling.getRecords().getFirst().getKey());
        } else {
            int parentKey = parent.getKey(parentIndex);
            int borrowedKey = rightSibling.removeKey(0);
            int borrowedChildPageNum = rightSibling.removeChildPageNum(0);

            node.addKey(parentKey);
            node.addChildPageNum(borrowedChildPageNum);
            parent.setKey(parentIndex, borrowedKey);

            BPlusTreeNode borrowedChild = loadNode(borrowedChildPageNum);
            borrowedChild.setParentPageNum(node.getPageNum());
            saveNode(borrowedChild);
        }

        saveNode(node);
        saveNode(rightSibling);
        saveNode(parent);
    }

    /**
     * 合并两个同层节点，将右节点并入左节点
     *
     * @param leftNode 左节点
     * @param rightNode 右节点
     * @param parent 父节点
     * @param parentIndex 父节点中的分隔键位置
     * @throws Exception 如果合并失败
     */
    private void mergeNodes(BPlusTreeNode leftNode, BPlusTreeNode rightNode, BPlusTreeNode parent,
                            int parentIndex) throws Exception {
        if (leftNode.isLeaf) {
            for (Record record : rightNode.getRecords()) {
                leftNode.addRecord(record);
            }

            leftNode.setNextLeafPageNum(rightNode.getNextLeafPageNum());
            if (rightNode.getNextLeafPageNum() != -1) {
                BPlusTreeNode nextLeaf = loadNode(rightNode.getNextLeafPageNum());
                nextLeaf.setPrevLeafPageNum(leftNode.getPageNum());
                saveNode(nextLeaf);
            }

            parent.removeKey(parentIndex);
        } else {
            int parentKey = parent.removeKey(parentIndex);
            leftNode.addKey(parentKey);
            for (int key : rightNode.keys) {
                leftNode.addKey(key);
            }
            for (int childPageNum : rightNode.getChildPageNums()) {
                leftNode.addChildPageNum(childPageNum);
                BPlusTreeNode childNode = loadNode(childPageNum);
                childNode.setParentPageNum(leftNode.getPageNum());
                saveNode(childNode);
            }
        }

        parent.removeChildPageNum(parentIndex + 1);
        saveNode(leftNode);
        saveNode(parent);

        if (parent.getPageNum() == rootPageNum) {
            shrinkRootIfNeeded(parent);
        } else if (parent.getKeyCount() < getMinKeys()) {
            handleUnderflow(parent);
        } else {
            refreshAncestorSeparators(parent);
        }
    }

    /**
     * 处理下溢
     *
     * @param node 下溢的节点
     * @throws Exception 如果处理失败
     */
    private void handleUnderflow(BPlusTreeNode node) throws Exception {
        int parentPageNum = node.getParentPageNum();
        if (parentPageNum == -1) {
            shrinkRootIfNeeded(node);
            return;
        }

        BPlusTreeNode parent = loadNode(parentPageNum);
        int index = findChildIndex(parent, node.getPageNum());
        if (index == parent.getChildPageNums().size()) {
            throw new IllegalStateException(
                    "父节点 " + parent.getPageNum() + " 中找不到子节点 " + node.getPageNum()
            );
        }

        if (index > 0) {
            BPlusTreeNode leftSibling = loadNode(parent.getChildPageNum(index - 1));
            int leftSize = leftSibling.isLeaf ? leftSibling.getRecords().size() : leftSibling.getKeyCount();
            if (leftSize > getMinKeys()) {
                borrowFromLeft(node, leftSibling, parent, index - 1);
                return;
            }
        }

        if (index < parent.getChildPageNums().size() - 1) {
            BPlusTreeNode rightSibling = loadNode(parent.getChildPageNum(index + 1));
            int rightSize = rightSibling.isLeaf ? rightSibling.getRecords().size() : rightSibling.getKeyCount();
            if (rightSize > getMinKeys()) {
                borrowFromRight(node, rightSibling, parent, index);
                return;
            }
        }

        if (index > 0) {
            BPlusTreeNode leftSibling = loadNode(parent.getChildPageNum(index - 1));
            mergeNodes(leftSibling, node, parent, index - 1);
        } else {
            BPlusTreeNode rightSibling = loadNode(parent.getChildPageNum(index + 1));
            mergeNodes(node, rightSibling, parent, index);
        }
    }

    /**
     * 打印树结构（调试用）
     *
     * @throws Exception 如果打印失败
     */
    public void printTree() throws Exception {
        printNode(rootPageNum, 0);
    }

    /**
     * 递归打印节点
     *
     * @param pageNum 节点页号
     * @param depth 深度
     * @throws Exception 如果打印失败
     */
    private void printNode(int pageNum, int depth) throws Exception {
        byte[] pageData = bufferPool.getPage(pageNum, pageManager);
        BPlusTreeNode node = pageSerializer.deserializeNode(pageData, pageNum);

        String indent = "  ".repeat(depth);

        if (node.isLeaf) {
            System.out.print(indent + "叶子节点[页" + pageNum + "]: ");
            for (Record record : node.getRecords()) {
                System.out.print("(" + record.getKey() + "," + record.getValue() + ") ");
            }
            System.out.println();
        } else {
            System.out.print(indent + "内部节点[页" + pageNum + "]: ");
            for (int key : node.keys) {
                System.out.print(key + " ");
            }
            System.out.println();

            for (int childPageNum : node.getChildPageNums()) {
                printNode(childPageNum, depth + 1);
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
     * 获取根节点页号
     *
     * @return 根节点页号
     */
    public int getRootPageNum() {
        return rootPageNum;
    }
}
