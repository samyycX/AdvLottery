package com.samyyc.lottery.configs;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.objects.LotteryResult;
import com.samyyc.lottery.objects.LotteryReward;
import com.samyyc.lottery.apis.CustomLotteryTask;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GlobalConfig {

    public static String PREFIX = TextUtil.convertColor("&e[&bAdvLottery&e] ");

    public static List<Player> rollingPlayerList = new ArrayList<>();

    public static HashMap<String, List<LotteryResult>> resultList = new HashMap<>();

    public static HashMap<String, LotteryReward> floorList = new HashMap<>();

    public static HashMap<String, List<String>> GUIScriptList = new HashMap<>();


    // api
    public static Map<String, CustomLotteryTask> taskMap = new HashMap<>();

    public static void putResult(String playername, LotteryResult result) {
        System.out.println(result.getLotteryReward().getRewardName()); //正常
        if (resultList.get(playername) != null) {
            resultList.get(playername).add(result);
        } else {
            List<LotteryResult> results = new ArrayList<>();
            results.add(result);
            System.out.println(results); //正常
            GlobalConfig.resultList.put(playername, results);
            System.out.println(GlobalConfig.resultList);
        }
    }

    public static List<LotteryResult> getResults(String playerName) {
        return resultList.get(playerName);
    }

}
