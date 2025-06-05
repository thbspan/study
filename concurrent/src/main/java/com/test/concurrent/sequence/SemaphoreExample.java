package com.test.concurrent.sequence;

import java.util.concurrent.Semaphore;

public class SemaphoreExample {


    public static void main(String[] args) {
        int loopCount = 10;
        Semaphore semA = new Semaphore(1);
        Semaphore semB = new Semaphore(0);
        Semaphore semC = new Semaphore(0);

        new Thread(() -> {
            for (int i = 0; i < loopCount; i++) {
                try {
                    semA.acquire();
                    System.out.print("A");
                    semB.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < loopCount; i++) {
                try {
                    semB.acquire();
                    System.out.print("B");
                    semC.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < loopCount; i++) {
                try {
                    semC.acquire();
                    System.out.println("C");
                    semA.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
