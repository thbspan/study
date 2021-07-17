package com.test.cglib.jdk;

public class RealSubject implements Subject {
    @Override
    public String sayHello(String name) {
        return "hi~ " + name;
    }

    @Override
    public String sayGoodBye(String name) {
        return sayHello(name) + " good bye";
    }
}
