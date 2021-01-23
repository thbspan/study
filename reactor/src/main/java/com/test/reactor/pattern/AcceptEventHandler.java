package com.test.reactor.pattern;

public class AcceptEventHandler extends EventHandler {

    private final Demultiplexer selector;

    public AcceptEventHandler(Demultiplexer selector) {
        this.selector = selector;
    }

    @Override
    public void handle(Event event) {

        if (event.type == EventType.ACCEPT){
            Event readEvent = new Event();
            readEvent.source = event.source;
            readEvent.type = EventType.READ;

            selector.addEvent(readEvent);
        }
    }
}
