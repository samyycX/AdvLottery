package com.samyyc.lottery.utils;

import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.objects.LotteryReward;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FloorUtil {

    public static boolean hasFloorReward(UUID uuid) {
        return (GlobalConfig.floorList.get(uuid) != null) && (GlobalConfig.floorList.get(uuid).size() != 0);
    }

    public static LotteryReward getFloorReward(UUID uuid) {
        if (GlobalConfig.floorList.get(uuid) != null && GlobalConfig.floorList.get(uuid).size() != 0) {
            LotteryReward reward = GlobalConfig.floorList.get(uuid).get(0);
            GlobalConfig.floorList.get(uuid).remove(0);
            return reward;
        } else {
            return null;
        }
    }

    public static void addFloorReward(UUID uuid, LotteryReward reward) {
        if (GlobalConfig.floorList.get(uuid) != null) {
            GlobalConfig.floorList.get(uuid).add(reward);
        } else {
            List<LotteryReward> rewardList = new ArrayList<>();
            rewardList.add(reward);
            GlobalConfig.floorList.put(uuid, rewardList);
        }
    }


}
