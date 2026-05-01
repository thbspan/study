package com.test.bplustree;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 页管理器：负责数据页的读取和写入
 * 模拟数据库的页管理机制，提供页级别的I/O操作
 *
 */
public class PageManager {
    /**
     * 页大小：4KB（标准数据库页大小）
     */
    public static final int PAGE_SIZE = 4096;

    /**
     * 文件头大小：64字节
     * 存储数据库的元数据信息
     */
    public static final int HEADER_SIZE = 64;

    /**
     * 数据文件路径
     */
    private final String dbFilePath;

    /**
     * 随机访问文件对象
     */
    private RandomAccessFile raf;

    /**
     * 构造函数
     *
     * @param dbFilePath 数据库文件路径
     */
    public PageManager(String dbFilePath) {
        this.dbFilePath = dbFilePath;
    }

    /**
     * 打开数据库文件
     *
     * @throws IOException 如果文件打开失败
     */
    public void open() throws IOException {
        raf = new RandomAccessFile(dbFilePath, "rw");
    }

    /**
     * 关闭数据库文件
     *
     * @throws IOException 如果文件关闭失败
     */
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    /**
     * 从文件读取指定页
     *
     * @param pageNum 页号（从0开始）
     * @return 页数据（字节数组）
     * @throws IOException 如果读取失败
     */
    public byte[] readPage(int pageNum) throws IOException {
        // 计算页在文件中的偏移量
        long offset = HEADER_SIZE + (long) pageNum * PAGE_SIZE;

        // 检查页是否存在
        long fileLength = raf.length();
        if (offset >= fileLength) {
            throw new IOException("页 " + pageNum + " 不存在，文件长度: " + fileLength);
        }

        // 读取页数据
        byte[] pageData = new byte[PAGE_SIZE];
        raf.seek(offset);
        raf.readFully(pageData);

        return pageData;
    }

    /**
     * 将页写入文件
     *
     * @param pageNum 页号
     * @param data    页数据
     * @throws IOException 如果写入失败
     */
    public void writePage(int pageNum, byte[] data) throws IOException {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("页数据大小必须为 " + PAGE_SIZE + " 字节");
        }

        // 计算页在文件中的偏移量
        long offset = HEADER_SIZE + (long) pageNum * PAGE_SIZE;

        // 写入页数据
        raf.seek(offset);
        raf.write(data);
    }

    /**
     * 分配新页
     * 在文件末尾分配一个新的页
     *
     * @return 新分配的页号
     * @throws IOException 如果分配失败
     */
    public int allocatePage() throws IOException {
        // 当前文件长度
        long fileLength = raf.length();

        // 计算新页号
        int newPageNum = (int) ((fileLength - HEADER_SIZE) / PAGE_SIZE);

        // 分配新页（写入空白页）
        byte[] emptyPage = new byte[PAGE_SIZE];
        writePage(newPageNum, emptyPage);

        return newPageNum;
    }

    /**
     * 释放页（将页标记为空白）
     * 注意：这里只是简单清零，实际数据库会维护空闲页链表
     *
     * @param pageNum 要释放的页号
     * @throws IOException 如果释放失败
     */
    public void freePage(int pageNum) throws IOException {
        byte[] emptyPage = new byte[PAGE_SIZE];
        writePage(pageNum, emptyPage);
    }

    /**
     * 获取总页数
     *
     * @return 数据库中的总页数
     * @throws IOException 如果读取失败
     */
    public int getTotalPages() throws IOException {
        long fileLength = raf.length();
        if (fileLength <= HEADER_SIZE) {
            return 0;
        }
        return (int) ((fileLength - HEADER_SIZE) / PAGE_SIZE);
    }

    /**
     * 初始化文件头
     * 在新建数据库时调用
     *
     * @throws IOException 如果写入失败
     */
    public void initHeader() throws IOException {
        byte[] header = new byte[HEADER_SIZE];

        // 魔数：0x42504C54 = "BPLT" (B+ Leaf Tree)
        header[0] = 0x42;
        header[1] = 0x50;
        header[2] = 0x4C;
        header[3] = 0x54;

        // 版本号：1
        header[4] = 0x00;
        header[5] = 0x00;
        header[6] = 0x00;
        header[7] = 0x01;

        // 根节点页号：0（第一个页）
        header[8] = 0x00;
        header[9] = 0x00;
        header[10] = 0x00;
        header[11] = 0x00;

        // 总页数：1
        header[12] = 0x00;
        header[13] = 0x00;
        header[14] = 0x00;
        header[15] = 0x01;

        // 写入文件头
        raf.seek(0);
        raf.write(header);
    }

    /**
     * 读取文件头
     *
     * @return 文件头数据
     * @throws IOException 如果读取失败
     */
    public byte[] readHeader() throws IOException {
        byte[] header = new byte[HEADER_SIZE];
        raf.seek(0);
        raf.readFully(header);
        return header;
    }

    /**
     * 更新文件头中的根节点页号
     *
     * @param rootPageNum 根节点页号
     * @throws IOException 如果写入失败
     */
    public void updateRootPageNum(int rootPageNum) throws IOException {
        raf.seek(8);
        raf.writeInt(rootPageNum);
    }

    /**
     * 读取文件头中的根节点页号
     *
     * @return 根节点页号
     * @throws IOException 如果读取失败
     */
    public int readRootPageNum() throws IOException {
        raf.seek(8);
        return raf.readInt();
    }

    /**
     * 更新文件头中的总页数
     *
     * @param totalPages 总页数
     * @throws IOException 如果写入失败
     */
    public void updateTotalPages(int totalPages) throws IOException {
        raf.seek(12);
        raf.writeInt(totalPages);
    }

    /**
     * 验证数据库文件是否有效
     *
     * @return 如果文件有效返回true
     * @throws IOException 如果读取失败
     */
    public boolean isValidDatabase() throws IOException {
        if (raf.length() < HEADER_SIZE) {
            return false;
        }

        byte[] header = readHeader();

        // 检查魔数
        return header[0] == 0x42 && header[1] == 0x50 &&
                header[2] == 0x4C && header[3] == 0x54;
    }
}
