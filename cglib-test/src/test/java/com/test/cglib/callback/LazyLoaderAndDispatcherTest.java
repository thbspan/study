package com.test.cglib.callback;

import org.junit.jupiter.api.Test;

import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LazyLoaderAndDispatcherTest {

    @Test
    public void test() {
        Bean bean = new Bean(1);
        assertEquals(1, bean.getId());
        bean.setId(2);
        assertEquals(2, bean.getId());
        AnotherBean anotherBean = bean.getAnotherBean();
        System.out.println(System.identityHashCode(anotherBean));
        System.out.println(anotherBean.getClass());
        System.out.println("=============");
        System.out.println(anotherBean.hashCode());
        System.out.println("=============");
        System.out.println(anotherBean);
        assertEquals(8, anotherBean.getValue());

        AnotherBean loadEveryTimeBean = bean.getLoadEveryTimeBean();
        System.out.println(loadEveryTimeBean.getClass());
        System.out.println(loadEveryTimeBean);
        assertEquals(6, loadEveryTimeBean.getValue());
    }
}

class Bean {
    private int id;
    private AnotherBean anotherBean;
    private AnotherBean loadEveryTimeBean;
    public Bean(int id) {
        this.id = id;
        // 类似单例模式
        this.anotherBean = lazyLoadBean();
        // 类似原型模式
        this.loadEveryTimeBean = everyTimeLazyLoadBean();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AnotherBean getAnotherBean() {
        return anotherBean;
    }

    public void setAnotherBean(AnotherBean anotherBean) {
        this.anotherBean = anotherBean;
    }

    public AnotherBean getLoadEveryTimeBean() {
        return loadEveryTimeBean;
    }

    public void setLoadEveryTimeBean(AnotherBean loadEveryTimeBean) {
        this.loadEveryTimeBean = loadEveryTimeBean;
    }

    private AnotherBean lazyLoadBean() {
        return (AnotherBean) Enhancer.create(AnotherBean.class, (LazyLoader) () -> {
            System.out.println("lazy loader start loading!");
            AnotherBean bean = new AnotherBean();
            bean.setValue(8);
            return bean;
        });
    }

    private AnotherBean everyTimeLazyLoadBean() {
        return (AnotherBean) Enhancer.create(AnotherBean.class, (Dispatcher) () -> {
            System.out.println("everytime loader start loading!");
            AnotherBean bean = new AnotherBean();
            bean.setValue(6);
            return bean;
        });
    }
}

class AnotherBean {
    private int value;

    public int getValue() {
        System.out.println("invoke get value");
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}