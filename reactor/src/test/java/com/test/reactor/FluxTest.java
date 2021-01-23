package com.test.reactor;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class FluxTest {

    @Test
    public void testJust() {
        Flux<Integer> flux = Flux.just(1, 2, 3);
        System.out.println(flux);
        // 订阅并打印数据流（有多个重载方法）
        flux.subscribe(System.out::println);
        flux.subscribe(System.out::println, System.out::println, () -> System.out.println("Completed!"));
        System.out.println(Flux.just());
    }

    @Test
    public void testError() {
        // 打印错误，不会打印完成信息
        Mono.error(new Exception("some error"))
                .subscribe(System.out::println,
                        System.err::println,
                        () -> System.out.println("Completed!"));
    }

    private Flux<Integer> generateFluxFrom1To6() {
        return Flux.just(1, 2, 3, 4, 5, 6);
    }

    private Mono<Integer> generateMonoWithError() {
        return Mono.error(new Exception("some error"));
    }

    @Test
    public void testStepVerifier() {
        StepVerifier.create(generateFluxFrom1To6())
                .expectNext(1, 2, 3, 4, 5, 6)
                .expectComplete()
                .verify();

        StepVerifier.create(generateMonoWithError())
                .expectErrorMessage("some error")
                .verify();
    }

    @Test
    public void testOperator() throws InterruptedException {
        // map flatMap like stream
        StepVerifier.create(Flux.range(1, 6)
                .map(i -> i * i))
                .expectNext(1, 4, 9, 16, 25, 36)
                .expectComplete()
                .verify();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Flux.just("flux", "mono")
                .flatMap(s -> Flux.fromArray(s.split(""))
                        // 每个元素延迟1s
                        .delayElements(Duration.ofSeconds(1)))
                // 不会消费数据流
                .doOnNext(System.out::print)
                .subscribe(System.out::println, System.err::println, countDownLatch::countDown);
        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    private Flux<String> getZipDescFlux() {
        String desc = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";
        return Flux.fromArray(desc.split("\\s+"));
    }

    @Test
    public void testZip() throws InterruptedException {
        // zip 能够合并多个流, zipWith非静态方法, 功能类似
        // 1、组成二元组
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Flux.zip(getZipDescFlux(),
                Flux.interval(Duration.ofMillis(100)))
                .subscribe(System.out::println, System.err::println, countDownLatch::countDown);
        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testSyncToAsync() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Mono.fromCallable(this::getStringSync)
                // 使用内置的弹性线程池执行
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(System.out::println, System.err::println, countDownLatch::countDown);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    private String getStringSync() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello, Reactor!";
    }

    @Test
    public void testSwitchScheduler() throws InterruptedException {
        // publishOn subscribeOn 都可以调整调度器
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Flux.range(1, 1000)
                .map(String::valueOf)
                // publishOn 会影响链中其后的操作符，所以filter是在boundedElastic线程中执行的
                .publishOn(Schedulers.boundedElastic()).filter(s -> s.length() >= 2)
                // publishOn 会影响链中其后的操作符，所以flatMap是在parallel线程中执行的
                .publishOn(Schedulers.parallel()).flatMap(s -> Flux.fromArray(s.split("")))
                // subscribeOn无论出现在什么位置，都只影响源头的执行环境，也就是range方法是执行在single单线程中的，直至被第一个publishOn切换调度器之前
                .subscribeOn(Schedulers.single())
                .subscribe(System.out::println, System.err::println, countDownLatch::countDown);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    /**
     * 异常处理
     */
    @Test
    public void testErrorHandle() {
        // 停止执行并打印异常
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);
        System.out.println();
        // 捕获异常并停止执行且返回一个静态的缺省值
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                .onErrorReturn(0)
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);
        System.out.println();
        // 捕获异常并停止执行且执行一个异常处理方法，并使用处理方法的返回值继续执行
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                .onErrorResume(e -> Mono.just(new Random().nextInt(10)))
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);
        System.out.println();
        // 捕获异常并停止执行且包装一个业务相关的异常，然后再抛出，和上面的类似
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                .onErrorResume(e -> Mono.error(new RuntimeException("test error", e)))
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);
        System.out.println("+++++++++");
        // 跳过错误使用原publisher继续执行
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                .onErrorContinue((e, o) -> {
                    System.out.println("*" + o);
                })
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);
        System.out.println("+++++++++");
        // 捕获，记录错误日志，然后继续抛出
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                .doOnError(e -> System.out.println("ERROR LOG " + e))
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);
        System.out.println("===========");
        // 出错后重试
        Flux.range(1, 6)
                .map(i -> 10 / (3 - i))
                .retry(1)
                .subscribe(System.out::println, System.err::println);
    }
}
