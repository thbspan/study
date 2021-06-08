package com.test.reactor.pattern;

import java.util.Date;

public class Source {
    private final Date date = new Date();

    private final String id = date + "_" + System.identityHashCode(date);

    @Override
    public String toString() {
        return id;
    }
}
