package com.test.bplustree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 页序列化器：负责B+树节点与字节数组之间的序列化/反序列化
 * 将内存中的节点结构转换为可存储的字节格式，以及反向操作
 *
 */
public class PageSerializer {

    /**
     * 页头大小：32字节
     */
    private static final int PAGE_HEADER_SIZE = 32;

    /**
     * 序列化内部节点到页
     *
     * @param keys          键列表
     * @param childPageNums 子节点页号列表
     * @param pageNum       当前页号
     * @param parentPageNum 父节点页号
     * @return 页数据（字节数组）
     */
    public byte[] serializeInternalNode(List<Integer> keys, List<Integer> childPageNums,
                                        int pageNum, int parentPageNum) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);

            // 写入页头
            dos.writeByte(0);  // 页类型：0 = 内部节点
            dos.writeShort(keys.size());  // 键数量
            dos.writeInt(parentPageNum);  // 父节点页号
            dos.writeInt(-1);  // 下一个叶子页号（内部节点为-1）
            dos.writeInt(-1);  // 前一个叶子页号（内部节点为-1）
            dos.write(new byte[17]);  // 保留17字节

            // 写入数据区：[键1][子页号1][键2][子页号2]...
            for (int i = 0; i < keys.size(); i++) {
                dos.writeInt(keys.get(i));  // 键
                dos.writeInt(childPageNums.get(i));  // 子页号
            }
            // 写入最后一个子页号
            dos.writeInt(childPageNums.getLast());

            // 填充剩余字节
            int writtenBytes = byteArrayOutputStream.size();
            int remaining = PageManager.PAGE_SIZE - writtenBytes;
            if (remaining > 0) {
                dos.write(new byte[remaining]);
            }

            dos.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("序列化内部节点失败", e);
        }
    }

    /**
     * 序列化叶子节点到页
     *
     * @param records       记录列表
     * @param pageNum       当前页号
     * @param parentPageNum 父节点页号
     * @param nextPageNum   下一个叶子页号
     * @param prevPageNum   前一个叶子页号
     * @return 页数据（字节数组）
     */
    public byte[] serializeLeafNode(List<TreeRecord> records, int pageNum,
                                    int parentPageNum, int nextPageNum, int prevPageNum) {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bas);

            // 写入页头
            dos.writeByte(1);  // 页类型：1 = 叶子节点
            dos.writeShort(records.size());  // 记录数量
            dos.writeInt(parentPageNum);  // 父节点页号
            dos.writeInt(nextPageNum);  // 下一个叶子页号
            dos.writeInt(prevPageNum);  // 前一个叶子页号
            dos.write(new byte[17]);  // 保留17字节

            // 写入数据区：[键1][值长度1][值数据1][键2][值长度2][值数据2]...
            for (TreeRecord record : records) {
                dos.writeInt(record.key());  // 键
                byte[] valueBytes = record.value().getBytes(StandardCharsets.UTF_8);
                dos.writeShort(valueBytes.length);  // 值长度
                dos.write(valueBytes);  // 值数据
            }

            // 填充剩余字节
            int writtenBytes = bas.size();
            int remaining = PageManager.PAGE_SIZE - writtenBytes;
            if (remaining > 0) {
                dos.write(new byte[remaining]);
            }

            dos.close();
            return bas.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("序列化叶子节点失败", e);
        }
    }

    /**
     * 从页数据反序列化内部节点
     *
     * @param pageData 页数据
     * @param pageNum  页号
     * @return 内部节点对象
     */
    public BPlusTreeNode deserializeInternalNode(byte[] pageData, int pageNum) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(pageData);
            DataInputStream dis = new DataInputStream(bais);

            // 读取页头
            byte pageType = dis.readByte();  // 应该是0
            short keyCount = dis.readShort();
            int parentPageNum = dis.readInt();
            int nextPageNum = dis.readInt();  // 内部节点为-1
            int prevPageNum = dis.readInt();  // 内部节点为-1
            dis.skipBytes(17);  // 跳过保留字节

            // 创建节点
            BPlusTreeNode node = new BPlusTreeNode(false);
            node.setPageNum(pageNum);
            node.setParentPageNum(parentPageNum);

            // 读取键和子页号
            for (int i = 0; i < keyCount; i++) {
                int key = dis.readInt();
                node.addKey(key);

                int childPageNum = dis.readInt();
                node.addChildPageNum(childPageNum);
            }
            // 读取最后一个子页号
            int lastChildPageNum = dis.readInt();
            node.addChildPageNum(lastChildPageNum);

            dis.close();
            return node;
        } catch (IOException e) {
            throw new RuntimeException("反序列化内部节点失败", e);
        }
    }

    /**
     * 从页数据反序列化节点（自动判断类型）
     *
     * @param pageData 页数据
     * @param pageNum  页号
     * @return 节点对象
     */
    public BPlusTreeNode deserializeNode(byte[] pageData, int pageNum) {
        // 读取页类型
        byte pageType = pageData[0];

        if (pageType == 0) {
            return deserializeInternalNode(pageData, pageNum);
        } else if (pageType == 1) {
            return deserializeLeafNode(pageData, pageNum);
        } else {
            throw new RuntimeException("未知的页类型: " + pageType);
        }
    }

    /**
     * 从页数据反序列化叶子节点
     *
     * @param pageData 页数据
     * @param pageNum  页号
     * @return 叶子节点对象
     */
    public BPlusTreeNode deserializeLeafNode(byte[] pageData, int pageNum) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(pageData);
            DataInputStream dis = new DataInputStream(bais);

            // 读取页头
            byte pageType = dis.readByte();  // 应该是1
            short recordCount = dis.readShort();
            int parentPageNum = dis.readInt();
            int nextPageNum = dis.readInt();
            int prevPageNum = dis.readInt();
            dis.skipBytes(17);  // 跳过保留字节

            // 创建节点
            BPlusTreeNode node = new BPlusTreeNode(true);
            node.setPageNum(pageNum);
            node.setParentPageNum(parentPageNum);
            node.setNextLeafPageNum(nextPageNum);
            node.setPrevLeafPageNum(prevPageNum);

            // 读取记录
            for (int i = 0; i < recordCount; i++) {
                int key = dis.readInt();
                short valueLength = dis.readShort();
                byte[] valueBytes = new byte[valueLength];
                dis.readFully(valueBytes);
                String value = new String(valueBytes, StandardCharsets.UTF_8);

                TreeRecord record = new TreeRecord(key, value);
                node.addRecord(record);
            }

            dis.close();
            return node;
        } catch (IOException e) {
            throw new RuntimeException("反序列化叶子节点失败", e);
        }
    }
}
