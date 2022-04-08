package com.samyyc.lottery.utils;

import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.objects.LotteryData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FloorUtil {

    public static boolean hasFloorData(UUID uuid) {
        return (GlobalConfig.floorList.get(uuid) != null) && (GlobalConfig.floorList.get(uuid).size() != 0);
    }

    public static LotteryData getFloorData(UUID uuid) {
        if (GlobalConfig.floorList.get(uuid) != null && GlobalConfig.floorList.get(uuid).size() != 0) {
            LotteryData data = GlobalConfig.floorList.get(uuid).get(0);
            GlobalConfig.floorList.get(uuid).remove(0);
            return data;
        } else {
            return null;
        }
    }

    public static void addFloorData(UUID uuid, LotteryData data) {
        if (GlobalConfig.floorList.get(uuid) != null) {
            GlobalConfig.floorList.get(uuid).add(data);
        } else {
            List<LotteryData> rewardList = new ArrayList<>();
            rewardList.add(data);
            GlobalConfig.floorList.put(uuid, rewardList);
        }
    }


}
