package com.samyyc.lottery.enums;


public enum Permission {

    PERM_DEFAULT("NULL"),

    PERM_HELP(PERM_DEFAULT),
    PERM_POOL_OPEN(PERM_DEFAULT),

    PERM_POOL_CREATE("bukkit.op"),
    PERM_POOL_ADDREWARD("bukkit.op"),
    PERM_POOL_SHOWLIST("bukkit.op"),

    PERM_REWARD_CREATE("bukkit.op"),
    PERM_REWARD_SETDISPLAYITEM("bukkit.op"),
    PERM_REWARD_SETITEM("bukkit.op"),
    PERM_REWARD_SHOWLIST("bukkit.op"),

    PERM_GROUP_CREATE("bukkit.op"),
    PERM_GROUP_ADD("bukkit.op"),
    PERM_GROUP_REMOVE("bukkit.op"),
    PERM_GROUP_SHOWLIST("bukkit.op"),

    PERM_INVENTORY_USE(PERM_DEFAULT),

    PERM_RELOAD("bukkit.op");

    private String perm;

    Permission(String perm) {
        this.perm = perm;
    }

    Permission(Permission perm) {
        this.perm = perm.perm();
    }

    public String perm() {
        return this.perm;
    }

}
