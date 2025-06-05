package com.test.concurrent.sequence;

import java.util.concurrent.SynchronousQueue;

public class SynchronousQueueExample {

    private static final Object OBJECT = new Object();

    public static void main(String[] args) throws InterruptedException {
        SynchronousQueue<Object> queueA = new SynchronousQueue<>();
        SynchronousQueue<Object> queueB = new SynchronousQueue<>();
        SynchronousQueue<Object> queueC = new SynchronousQueue<>();

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
                    if (i < loopCount - 1) {
                        queueA.put(OBJECT);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }).start();

        queueA.put(OBJECT);
    }

}
