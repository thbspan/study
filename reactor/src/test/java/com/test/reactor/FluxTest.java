package com.test.reactor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class FluxTest {

    @Test
    public void testJust() {
        // 开启调试模式
        Hooks.onOperatorDebug();
        Flux<Integer> flux = Flux.just(1, 2, 3)
                // 帮助问题定位，可以打印调试信息
                .checkpoint()
                // 记录日志
                .log();
        System.out.println(flux);
        // 订阅并打印数据流（有多个重载方法）
        flux.subscribe(System.out::println);
        flux.subscribe(System.out::println, System.out::println, () -> System.out.println("Completed!"));
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
                .subscribe(System.out::println, throwable -> {
                    System.err.println(throwable);
                    countDownLatch.countDown();
                }, countDownLatch::countDown);
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

    @Test
    public void testSubscribe() {
        Flux<Integer> ints = Flux.range(1, 4);
        ints.subscribe(System.out::println,
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"),
                // 从源数据中获取最多多少个元素
                sub -> sub.request(10));

        Flux.range(1, 4)
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        System.out.println("Subscribed");
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        System.out.println(value);
                        request(1);
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        super.hookOnError(throwable);
                    }

                    @Override
                    protected void hookOnCancel() {
                        System.out.println("Canceled!");
                    }
                });
    }

    @Test
    public void testBuffer() {
        Flux<String> stringFlux = Flux.just("a", "b", "c", "d", "e", "f", "g");
        StepVerifier.create(stringFlux)
                .expectNext("a", "b", "c", "d", "e", "f", "g")
                .verifyComplete();
        // 缓冲
        StepVerifier.create(stringFlux.buffer(2))
                .expectNext(Arrays.asList("a", "b"), Arrays.asList("c", "d"), Arrays.asList("e", "f"),
                        Collections.singletonList("g"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testConcurrencyAndPrefetch() {
        int concurrency = 3;
        int prefetch = 6;
        Flux.range(1, 100)
                .log()
                .flatMap(i -> Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).log(),
                        concurrency, prefetch)
                .subscribe();

    }

    @Test
    public void testBackpressure() {
        Flux<String> flux = Flux.range(1, 10)
                .map(String::valueOf)
                .log();
        flux.subscribe(new Subscriber<String>() {
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 2;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(requestCount);  // 启动
            }

            @Override
            public void onNext(String s) {
                count++;
                if (count == requestCount) {  // 通过count控制每次request两个元素
                    try {
                        // 处理完两个元素后休息一下
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    subscription.request(requestCount);
                    count = 0;
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
        System.out.println("======================");
        // 使用 BaseSubscriber 实现 Subscription 类似的功能
        flux.subscribe(new BaseSubscriber<String>() {
            private final int requestCount = 2;
            private int count = 0;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(String value) {
                count++;
                if (count == requestCount) { // 通过count控制每次request两个元素
                    try {
                        // 处理完两个元素后休息一下
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    request(requestCount);
                    count = 0;
                }
            }
        });
        System.out.println("======================");
        // 使用limitRate
        flux.limitRate(2)
                .subscribe();
        System.out.println("======================");
        // 限制处理元素的数量
        flux.limitRequest(3)
                .subscribe();
    }

    @Test
    public void testConcatMap() {
        Student st1 = new Student("张三", 90);
        Student st2 = new Student("李四", 91);
        Student st3 = new Student("王二", 87);
        Student st4 = new Student("李逵", 97);
        Student st5 = new Student("宋江", 85);
        List<Student> list1 = new ArrayList<>();
        list1.add(st1);
        list1.add(st2);
        list1.add(st5);
        List<Student> list2 = new ArrayList<>();
        list2.add(st3);
        list2.add(st4);
        Teacher t1 = new Teacher(list1);
        Teacher t2 = new Teacher(list2);
        List<Teacher> teachers = Arrays.asList(t1, t2);
        Flux.fromIterable(teachers).concatMap(t -> Flux.fromIterable(t.getStudents()))
                .sort(Comparator.comparingInt(Student::getAge))
                .subscribe(System.out::println);

    }

    static class Student {
        private final String name;
        private final int age;

        public Student(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Student.class.getSimpleName() + "[", "]")
                    .add("name='" + name + "'")
                    .add("age=" + age)
                    .toString();
        }
    }

    static class Teacher {
        private final List<Student> students;

        public Teacher(List<Student> students) {
            this.students = students;
        }

        public List<Student> getStudents() {
            return students;
        }
    }
}
