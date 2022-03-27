package com.samyyc.lottery.containers;

import com.samyyc.lottery.objects.LotteryPool;

import java.util.HashMap;
import java.util.Map;

public class PoolContainer {

    private static final Map<String, LotteryPool> POOL_MAP = new HashMap<>();

    public static LotteryPool getPool(String poolName) {
        if (POOL_MAP.get(poolName) == null) {
            addPool(poolName, false);
        }
        return POOL_MAP.get(poolName);
    }

    public static void addPool(String poolName, boolean op) {
        LotteryPool pool = new LotteryPool(poolName, op);
        POOL_MAP.put(poolName, pool);
    }

    public static void destroy() {
        PoolContainer.POOL_MAP.clear();
    }

}
