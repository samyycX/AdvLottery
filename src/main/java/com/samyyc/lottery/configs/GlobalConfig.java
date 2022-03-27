package com.samyyc.lottery.configs;

import com.samyyc.lottery.objects.LotteryResult;
import com.samyyc.lottery.objects.LotteryReward;
import com.samyyc.lottery.apis.CustomLotteryTask;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.entity.Player;

import java.util.*;

public class GlobalConfig {

    public static String PREFIX = TextUtil.convertColor("&e[&bAdvLottery&e] ");

    public static LinkedList<Player> rollingPlayerList = new LinkedList<>();

    public static Map<String, List<LotteryResult>> resultList = new HashMap<>();

    public static HashMap<String, LotteryReward> floorList = new HashMap<>();

    public static HashMap<String, List<String>> GUIScriptList = new HashMap<>();


    // api
    public static Map<String, CustomLotteryTask> taskMap = new HashMap<>();

}
