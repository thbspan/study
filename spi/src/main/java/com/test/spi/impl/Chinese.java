package com.test.spi.impl;

import com.test.spi.People;

public class Chinese implements People {
    @Override
    public String speak() {
        return "speak chinese";
    }
}
