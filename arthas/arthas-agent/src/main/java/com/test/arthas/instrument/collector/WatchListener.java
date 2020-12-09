package com.test.arthas.instrument.collector;

import com.test.arthas.instrument.advice.Advice;

public interface WatchListener {
    void call(String key, Advice advice) throws Exception;
}