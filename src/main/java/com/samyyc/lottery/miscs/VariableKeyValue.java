package com.samyyc.lottery.miscs;

public class VariableKeyValue {

    private String key;
    private int value;

    public VariableKeyValue(){}

    public VariableKeyValue(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public VariableKeyValue setKey(String key) {
        this.key = key;
        return this;
    }

    public int value() {
        return value;
    }

    public VariableKeyValue setValue(int value) {
        this.value = value;
        return this;
    }
}
