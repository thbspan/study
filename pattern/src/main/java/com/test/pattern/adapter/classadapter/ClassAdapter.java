package com.test.pattern.adapter.classadapter;

/**
 * 适配器
 */
public class ClassAdapter extends Source implements Target {
    @Override
    public int output5V() {
        System.out.println("通过220V转换得到5v");
        return output220V()/44;
    }
}
