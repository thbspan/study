package com.test.mybatis.autoconstructor;

import java.util.Date;

public class PrimitiveSubject {
    private final int id;
    private final String name;
    private final int age;
    private final int height;
    private final int weight;
    private final boolean active;
    private final Date dt;

    public PrimitiveSubject(final int id, final String name, final int age, final int height, final int weight,
                            final boolean active, final Date dt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.active = active;
        this.dt = dt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isActive() {
        return active;
    }

    public Date getDt() {
        return dt;
    }
}
