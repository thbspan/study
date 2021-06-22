package com.test.rxjava;

import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ListFileTest {

    @Test
    public void test() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Observable.fromArray(Paths.get("."))
                .flatMap(path -> Observable.fromStream(Files.walk(path, FileVisitOption.FOLLOW_LINKS)))
                .map(Path::toRealPath)
                .map(Path::toString)
                .filter(name -> {
                    if (name.endsWith(".csv")) {
                        // throw new IllegalStateException("for test");
                    }
                    return name.endsWith(".java");
                })
                // 指定 Subscriber 的回调发生在新线程中，即 println 发生在新线程中（事件消费线程）
                // 可以指定多次，实现多个操作在不同线程中执行
                .observeOn(Schedulers.newThread())
                // 指定 subscribe() 发生在 IO 线程(事件产生线程)
                .subscribeOn(Schedulers.io())
                .subscribe(s -> System.out.printf("thread name:%s value:%s%n", Thread.currentThread().getName(), s), throwable -> {
                    System.err.println(throwable);
                    countDownLatch.countDown();
                }, countDownLatch::countDown);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testSchedulers() {
        Observable.just(1, 2, 3, 4) // IO 线程，由 subscribeOn() 指定
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map(String::valueOf) // 新线程，由 observeOn() 指定
                .observeOn(Schedulers.io())
                .map(Integer::valueOf) // IO 线程，由 observeOn() 指定
                .observeOn(Schedulers.computation())
                .subscribe(System.out::println);  // 计算线程，由 observeOn() 指定
    }
}
