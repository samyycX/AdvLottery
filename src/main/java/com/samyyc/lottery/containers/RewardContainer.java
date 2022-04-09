package com.samyyc.lottery.containers;

import com.samyyc.lottery.objects.LotteryReward;

import java.util.HashMap;
import java.util.Map;

public class RewardContainer {

    private static final Map<String, LotteryReward> REWARD_MAP = new HashMap<>();

    public static LotteryReward getReward(String rewardName) {
        if (!rewardName.startsWith("奖品组#") && !REWARD_MAP.containsKey(rewardName)) {
            addReward(rewardName);
        }
        return REWARD_MAP.get(rewardName);
    }

    public static void addReward(String rewardName) {
        LotteryReward reward = new LotteryReward(rewardName);
        REWARD_MAP.put(rewardName, reward);
    }

    public static void destroy() {
        RewardContainer.REWARD_MAP.clear();
    }


}
