package com.test.reactor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyEventSource {
    private final List<MyEventListener> listeners = new ArrayList<>();

    public void register(MyEventListener listener) {
        listeners.add(listener);
    }

    public void newEvent(MyEvent event) {
        for (MyEventListener listener : listeners) {
            listener.onNewEvent(event);
        }
    }

    public void eventStopped() {
        for (MyEventListener listener : listeners) {
            listener.onEventStopped();
        }
    }

    public static class MyEvent {
        private Date timeStamp;
        private String message;

        public MyEvent() {
        }

        public MyEvent(Date timeStamp, String message) {
            this.timeStamp = timeStamp;
            this.message = message;
        }

        public Date getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(Date timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
