package com.test.rsocket.example.fnf;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

public class TaskProcessingWithServerSideNotificationsExample {

    public static void main(String[] args) throws InterruptedException {
        Sinks.Many<Task> tasksProcessor = Sinks
                .many()
                .unicast()
                .onBackpressureBuffer(Queues.<Task>unboundedMultiproducer().get());
        ConcurrentMap<String, BlockingQueue<Task>> idToCompletedTasksMap = new ConcurrentHashMap<>();
        ConcurrentMap<String, RSocket> idToRSocketMap = new ConcurrentHashMap<>();

        new BackgroundWorker(tasksProcessor.asFlux(), idToCompletedTasksMap, idToRSocketMap);

        RSocketServer.create(new TasksAcceptor(tasksProcessor, idToCompletedTasksMap, idToRSocketMap))
                .bindNow(TcpServerTransport.create(9991));

        Mono<RSocket> rSocketMono = RSocketConnector.create()
                .setupPayload(DefaultPayload.create("Test"))
                .acceptor(SocketAcceptor.forFireAndForget(payload -> {
                    System.out.printf("Received Processed Task[%s]\n", payload.getDataUtf8());
                    payload.release();
                    return Mono.empty();
                }))
                .connect(TcpClientTransport.create(9991));
        RSocket rSocketRequester = rSocketMono.block();
        if (rSocketRequester != null) {
            for (int i = 0; i < 10; i++) {
                rSocketRequester.fireAndForget(DefaultPayload.create("task" + i)).block();
            }
            TimeUnit.SECONDS.sleep(4);
            rSocketRequester.dispose();
            System.out.println("Disposed");
        }
        TimeUnit.SECONDS.sleep(4);
        rSocketRequester = rSocketMono.block();
        if (rSocketRequester != null) {
            System.out.println("Reconnected");
            TimeUnit.SECONDS.sleep(10);
        }
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

            if (rSocket != null) {
                rSocket.fireAndForget(DefaultPayload.create("completed-"+task.content))
                        .subscribe(null, Throwable::printStackTrace, () -> completedTasksQueue.remove(task));
            }
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

    static class TasksAcceptor implements SocketAcceptor {
        final Sinks.Many<Task> tasksToProcess;
        final ConcurrentMap<String, BlockingQueue<Task>> idToCompletedTasksMap;
        final ConcurrentMap<String, RSocket> idToRSocketMap;

        public TasksAcceptor(Sinks.Many<Task> tasksToProcess,
                             ConcurrentMap<String, BlockingQueue<Task>> idToCompletedTasksMap,
                             ConcurrentMap<String, RSocket> idToRSocketMap) {
            this.tasksToProcess = tasksToProcess;
            this.idToCompletedTasksMap = idToCompletedTasksMap;
            this.idToRSocketMap = idToRSocketMap;
        }

        @Override
        public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
            String id = setup.getDataUtf8();

            if (this.idToRSocketMap.compute(id, (__, old) -> old == null || old.isDisposed() ? sendingSocket : old) == sendingSocket) {
                return Mono.<RSocket>just(new RSocketTaskHandler(idToRSocketMap, tasksToProcess, id, sendingSocket))
                        .doOnSuccess(__ -> checkTasksToDeliver(sendingSocket, id));
            }

            return Mono.error(new IllegalStateException("There is already a client connected with the same ID"));
        }

        private void checkTasksToDeliver(RSocket sendingSocket, String id) {
            BlockingQueue<Task> tasksToDeliver = this.idToCompletedTasksMap.get(id);
            if (tasksToDeliver == null || tasksToDeliver.isEmpty()) {
                return;
            }
            for (; ; ) {
                Task task = tasksToDeliver.poll();

                if (task == null) {
                    return;
                }
                sendingSocket.fireAndForget(DefaultPayload.create("recover-completed-" + task.content))
                        .subscribe(null, e -> tasksToDeliver.offer(task));
            }
        }
    }

    static class RSocketTaskHandler implements RSocket {
        private final String id;
        private final RSocket sendingSocket;
        private final ConcurrentMap<String, RSocket> idToRSocketMap;
        private final Sinks.Many<Task> tasksToProcess;

        public RSocketTaskHandler(ConcurrentMap<String, RSocket> idToRSocketMap,
                                  Sinks.Many<Task> tasksToProcess,
                                  String id,
                                  RSocket sendingSocket) {
            this.id = id;
            this.sendingSocket = sendingSocket;
            this.idToRSocketMap = idToRSocketMap;
            this.tasksToProcess = tasksToProcess;
        }

        @Override
        public Mono<Void> fireAndForget(Payload payload) {
            Sinks.EmitResult result = tasksToProcess.tryEmitNext(new Task(id, payload.getDataUtf8()));
            payload.release();
            return result.isFailure() ? Mono.error(new Sinks.EmissionException(result)) : Mono.empty();
        }

        @Override
        public void dispose() {
            idToRSocketMap.remove(id, sendingSocket);
        }
    }
}
