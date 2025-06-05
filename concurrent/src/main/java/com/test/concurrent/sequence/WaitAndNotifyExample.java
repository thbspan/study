package com.test.concurrent.sequence;

import java.util.concurrent.atomic.AtomicInteger;

public class WaitAndNotifyExample {

    private static final Object LOCK = new Object();
    private static final AtomicInteger count = new AtomicInteger(); // 0: K, 1: U, 2: C
    private static final int LOOP_COUNT = 10;

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> printChar("A", 0));
        Thread t2 = new Thread(() -> printChar("B", 1));
        Thread t3 = new Thread(() -> printChar("C", 2));

        t1.start();
        t2.start();
        t3.start();
    }

    private static void printChar(String ch, int threadState) {
        for (int i = 0; i < LOOP_COUNT; i++) {
            synchronized (LOCK) {
                while (count.get() % 3 != threadState) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.print(ch);
                if (threadState == 2) {
                    System.out.println(); // 换行，每次输出KUC后换行
                }
                count.incrementAndGet();
                LOCK.notifyAll();
            }
        }
    }
}
