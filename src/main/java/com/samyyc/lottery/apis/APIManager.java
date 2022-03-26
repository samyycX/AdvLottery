package com.samyyc.lottery.apis;

import com.samyyc.lottery.configs.GlobalConfig;

public class APIManager {

    public void registerAdvLotteryTask(CustomLotteryTask task) {
        String identifier = task.getIdentifier();
        GlobalConfig.taskMap.put(identifier, task);
    }

}
