package com.test.rxjava;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import io.reactivex.rxjava3.core.Observable;

public class ObservableTest {

    private Observable<String> observable;
    private CountDownLatch countDownLatch;

    @BeforeEach
    public void beforeEach() {
        countDownLatch = new CountDownLatch(1);
    }

    @Test
    public void testCreate() {
        observable = Observable.create(emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("World");
            emitter.onNext(":)");
            emitter.onComplete();
        });
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/csv/rxjava-just.csv")
    public void testJust(String item0, String item1) {
        observable = Observable.just(item0, item1);
    }

    @AfterEach
    public void afterEach() throws InterruptedException {
        observable.subscribe(System.out::println, throwable -> {
            System.err.println(throwable);
            countDownLatch.countDown();
        }, countDownLatch::countDown);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}
