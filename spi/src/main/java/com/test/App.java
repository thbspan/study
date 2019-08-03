package com.test;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.test.spi.People;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        ServiceLoader<People> peoples = ServiceLoader.load(People.class);
        for (People people : peoples) {
            System.out.println(people.speak());
        }
    }
}
