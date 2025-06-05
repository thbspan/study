package com.test.concurrent.sequence;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockQueueExample {

    private static final Object OBJECT = new Object();

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Object> queueA = new ArrayBlockingQueue<>(1);
        BlockingQueue<Object> queueB = new ArrayBlockingQueue<>(1);
        BlockingQueue<Object> queueC = new ArrayBlockingQueue<>(1);

        int loopCount = 10;
        new Thread(() -> {
            for (int i = 0; i < loopCount; i++) {
                try {
                    queueA.take();
                    System.out.print("A");
                    queueB.put(OBJECT);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < loopCount; i++) {
                try {
                    queueB.take();
                    System.out.print("B");
                    queueC.put(OBJECT);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < loopCount; i++) {
                try {
                    queueC.take();
                    System.out.println("C");
                    queueA.put(OBJECT);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }).start();

        queueA.put(OBJECT);
    }
}
