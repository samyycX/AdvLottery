package com.samyyc.lottery.configs;

import com.samyyc.lottery.objects.LotteryData;
import com.samyyc.lottery.objects.LotteryResult;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.entity.Player;

import java.util.*;

public class GlobalConfig {

    public static String PREFIX = TextUtil.convertColor("&e[&bAdvLottery&e] ");

    public static List<Player> rollingPlayerList = new ArrayList<>();

    public static HashMap<UUID, List<LotteryResult>> resultList = new HashMap<>();

    public static HashMap<UUID, List<LotteryData>> floorList = new HashMap<>();

    public static HashMap<String, List<String>> GUIScriptList = new HashMap<>();

    public static Map<UUID, String> TEMP = new HashMap<>();

    public static void putResult(UUID uuid, LotteryResult result) {
        if (resultList.get(uuid) != null) {
            resultList.get(uuid).add(result);
        } else {
            List<LotteryResult> results = new ArrayList<>();
            results.add(result);
            System.out.println(results); //正常
            GlobalConfig.resultList.put(uuid, results);
            System.out.println(GlobalConfig.resultList);
        }
    }

    public static List<LotteryResult> getResults(UUID playerUUID) {
        return resultList.get(playerUUID);
    }

    public static void destroy() {
        rollingPlayerList.clear();
        resultList.clear();
        GUIScriptList.clear();
    }

}
