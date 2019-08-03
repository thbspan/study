package com.test.spi.impl;

import com.test.spi.People;

public class American implements People {
    @Override
    public String speak() {
        return "speak English";
    }
}
