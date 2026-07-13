package com.test.redis;

import java.security.SecureRandom;
import java.util.Random;

/**
 * redis zset 数据结构的一部分(hashmap + SkipList)
 */
public class SkipList {

    private static final int MAX_LEVEL = 32;

    private static final double P = 0.25;

    private final Random random;

    static class Node {
        String value;
        double score;

        Node backward; // 仅 level0 用（方便反向遍历）

        Level[] levels;

        static class Level {
            Node forward;
            int span; // 跨度（用于 rank）
        }

        public Node(int level, double score, String value) {
            this.score = score;
            this.value = value;
            this.levels = new Level[level];
            for (int i = 0; i < level; i++) {
                levels[i] = new Level();
            }
        }
    }

    private final Node head;

    private Node tail;

    private int level; // 当前最大层

    private int length;

    public SkipList() {
        level = 1;
        length = 0;
        random = new SecureRandom();
        head = new Node(MAX_LEVEL, 0, null);
    }

    private int randomLevel() {
        int level = 1;
        while (random.nextDouble() < P && level < MAX_LEVEL) {
            level++;
        }
        return level;
    }

    public void insert(double score, String value) {
        Node[] update = new Node[MAX_LEVEL];
        int[] rank = new int[MAX_LEVEL];

        Node x = head;

        // 1. 查找插入位置
        for (int i = level - 1; i > 0; i--) {
            rank[i] = (i == level - 1) ? 0 : rank[i + 1];
            while (x.levels[i].forward != null &&
                    compare(x.levels[i].forward, score, value) < 0) {
                rank[i] = x.levels[i].span;
                x = x.levels[i].forward;
            }
            update[i] = x;
        }
    }

    private int compare(Node node, double score, String value) {
        if (node.score == score) {
            return node.value.compareTo(value);
        }
        return Double.compare(node.score, score);
    }
}