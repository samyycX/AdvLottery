package com.samyyc.lottery.apis;

import java.util.HashMap;
import java.util.Map;

public class APIContainer {

    public static Map<String, ILotteryTask> taskMap = new HashMap<>();
    public static Map<String, ILotteryReward> preRewardMap = new HashMap<>();
    public static Map<String, ILotteryReward> rewardMap = new HashMap<>();

}
