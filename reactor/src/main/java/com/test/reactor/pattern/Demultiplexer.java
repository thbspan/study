package com.test.reactor.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Demultiplexer {
    private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

    private final Object lock = new Object();

    public List<Event> select(){
        return select(0);
    }

    private List<Event> select(int timeout) {
        if (timeout > 0){
            if (eventQueue.isEmpty()){
                synchronized (lock){
                    if (eventQueue.isEmpty()) {
                        try {
                            lock.wait(timeout);
                        } catch (InterruptedException e){
                            // ignore it
                        }
                    }
                }
            }
        }
        List<Event> events = new ArrayList<>();
        eventQueue.drainTo(events);
        return events;
    }

    public void addEvent(Event e){
        if (eventQueue.offer(e)){
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
