package com.samyyc.lottery.miscs;

public class KV {

    private String key;
    private int value;

    public KV(){}

    public KV(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public KV setKey(String key) {
        this.key = key;
        return this;
    }

    public int value() {
        return value;
    }

    public KV setValue(int value) {
        this.value = value;
        return this;
    }
}
