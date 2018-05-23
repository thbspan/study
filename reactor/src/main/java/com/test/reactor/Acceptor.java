package com.test.reactor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Acceptor implements Runnable {
    private final int port;

    private final Demultiplexer selector;

    private BlockingQueue<Source> sourceQueue = new LinkedBlockingQueue<>();

    public Acceptor(Demultiplexer selector, int port) {
        this.port = port;
        this.selector = selector;
    }

    public void aNewConnection(Source source){
        sourceQueue.offer(source);
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {

        while (true){

            Source source = null;
            try {
                // 相当于 serversocket.accept()
                source = sourceQueue.take();
            } catch (InterruptedException e) {
                // ignore it;
            }

            if (source != null) {
                Event acceptEvent = new Event();
                acceptEvent.source = source;
                acceptEvent.type = EventType.ACCEPT;

                selector.addEvent(acceptEvent);
            }
        }
    }
}
