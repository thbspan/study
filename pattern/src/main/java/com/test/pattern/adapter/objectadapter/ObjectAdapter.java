package com.test.pattern.adapter.objectadapter;

import com.test.pattern.adapter.classadapter.Source;
import com.test.pattern.adapter.classadapter.Target;

/**
 * 对象适配器
 */
public class ObjectAdapter implements Target {
    private Source source;

    public ObjectAdapter(Source source) {
        this.source = source;
    }

    @Override
    public int output5V() {
        return source.output220V() / 44;
    }
}
