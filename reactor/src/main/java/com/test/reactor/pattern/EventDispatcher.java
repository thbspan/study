package com.test.reactor.pattern;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventDispatcher {

    private final Map<EventType, EventHandler> eventHandlerMap = new ConcurrentHashMap<>();


    private final Demultiplexer selector;

    public EventDispatcher(Demultiplexer selector) {
        this.selector = selector;
    }

    public void registEventHandler(EventType eventType, EventHandler eventHandler) {
        eventHandlerMap.put(eventType, eventHandler);
    }

    public void removeEventHandler(EventType eventType) {
        eventHandlerMap.remove(eventType);
    }

    public void handleEvents() {
        dispatch();
    }

    private void dispatch() {
        while (true) {
            List<Event> events = selector.select();

            for (Event event : events) {
                EventHandler eventHandler = eventHandlerMap.get(event.type);
                eventHandler.handle(event);
            }
        }
    }
}
