package com.test.reactor.pattern;

public abstract class EventHandler {

    private Source source;

    public abstract void handle(Event event);

    public Source getSource(){
        return source;
    }
}
