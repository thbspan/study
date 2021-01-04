package com.test.zookeeper.domain;

public class ServerPayload {
    private String name;
    private int payload;

    public ServerPayload() {
    }

    public ServerPayload(String name, int payload) {
        this.name = name;
        this.payload = payload;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPayload() {
        return payload;
    }

    public void setPayload(int payload) {
        this.payload = payload;
    }
}
