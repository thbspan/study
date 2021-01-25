package com.test.rsocket.example.fnf;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import io.rsocket.RSocket;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

public class TaskProcessingWithServerSideNotificationsExample {
    public static void main(String[] args) {
        Sinks.Many<Task> tasksProcessor = Sinks
                .many()
                .unicast()
                .onBackpressureBuffer(Queues.<Task>unboundedMultiproducer().get());
        ConcurrentMap<String, BlockingQueue<Task>> idToCompletedTasksMap = new ConcurrentHashMap<>();
        ConcurrentMap<String, RSocket> idToRSocketMap = new ConcurrentHashMap<>();

        BackgroundWorker backgroundWorker =
                new BackgroundWorker(tasksProcessor.asFlux(), idToCompletedTasksMap, idToRSocketMap);
    }


    static class BackgroundWorker extends BaseSubscriber<Task> {
        final ConcurrentMap<String, BlockingQueue<Task>> idToCompletedTasksMap;
        final ConcurrentMap<String, RSocket> idToRSocketMap;

        BackgroundWorker(Flux<Task> taskProducer,
                         ConcurrentMap<String, BlockingQueue<Task>> idToCompletedTasksMap,
                         ConcurrentMap<String, RSocket> idToRSocketMap) {
            this.idToCompletedTasksMap = idToCompletedTasksMap;
            this.idToRSocketMap = idToRSocketMap;
            taskProducer.concatMap(t -> Mono.delay(Duration.ofMillis(ThreadLocalRandom.current().nextInt(200, 2000)))
                    .thenReturn(t))
                    .subscribe(this);
        }

        @Override
        protected void hookOnNext(Task task) {
            BlockingQueue<Task> completedTasksQueue = idToCompletedTasksMap.computeIfAbsent(task.id, __ -> new LinkedBlockingQueue<>());
            completedTasksQueue.offer(task);
            RSocket rSocket = idToRSocketMap.get(task.id);

            super.hookOnNext(task);
        }
    }

    static class Task {
        final String id;
        final String content;

        Task(String id, String content) {
            this.id = id;
            this.content = content;
        }
    }
}
