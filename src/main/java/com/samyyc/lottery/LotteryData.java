package com.samyyc.lottery;

import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.utils.Calculator;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LotteryData {

    String poolName;
    public LotteryReward reward;
    YamlConfiguration poolConfig;
    YamlConfiguration dataConfig;
    File file;
    Map<String, Object> configMap = new LinkedHashMap<>();
    int totaltime = 0;
    Map<String, Map<String, Object>> playerDataMap = new LinkedHashMap<>();
    List<String> customLores = new LinkedList<>();
    boolean isEnabled = true;
    ItemStack displayItemstack;

    Player player;

    int poolTotalTime = 0;
    Map<String, Integer> poolPlayerTotalTime = new HashMap<>();
    List<String> requirementList = new ArrayList<>();
    int serverRequirementLength = 0;

    public LotteryData(YamlConfiguration poolConfig, String poolName, String rewardName, Player player) {
        this.poolConfig = poolConfig;
        this.player = player;
        this.poolName = poolName;
        reward = new LotteryReward(rewardName);
        file = new File(Lottery.getInstance().getDataFolder(), "数据/"+poolName+".yml");
        if (!file.exists()) {
            refreshFile(poolName, file);
        }

        // 固定配置
        ConfigurationSection configSection = poolConfig.getConfigurationSection("奖品列表."+rewardName);
        for ( String key : configSection.getKeys(false)) {
            switch (key) {
                case "真实概率":
                    int amount = configSection.getInt(key);
                    configMap.put(key, amount);
                    break;
                case "显示概率":
                    String text = TextUtil.convertColor(configSection.getString(key));
                    configMap.put(key, text);
                    break;
                case "全服限制数量":
                case "玩家限制数量":
                    int amount0 = configSection.getInt(key);
                    if (amount0 != -1 && amount0 != 0) {
                        configMap.put(key, amount0);
                    }
                    break;
                case "保底次数":
                case "保底限量":
                    int amount1 = configSection.getInt(key);
                    if ( amount1 != 0 && amount1 != -1) {
                        configMap.put(key, amount1);
                    }
                    break;
                case "全服限制开启条件":
                    requirementList = configSection.getStringList(key);
                    break;
                case "玩家限制开启条件":
                    requirementList.addAll(configSection.getStringList(key));
                case "lores":
                    customLores = configSection.getStringList(key);
                    break;
            }
        }
        // 数据

        dataConfig = YamlConfiguration.loadConfiguration(file);
        poolTotalTime = dataConfig.getInt("全服已抽此奖池次数");
        ConfigurationSection playerTotalSection = dataConfig.getConfigurationSection("玩家已抽此奖池次数");
        for (String key : playerTotalSection.getKeys(false)) {
            poolPlayerTotalTime.put(key, playerTotalSection.getInt(key));
        }
        ConfigurationSection dataSection = dataConfig.getConfigurationSection("奖品数据."+rewardName);

        for ( String key : dataSection.getKeys(false) ) {
            if ("全服已出此奖品次数".equals(key)) {
                totaltime = configSection.getInt(key);
            } else if ("玩家数据".equals(key)) {
                ConfigurationSection playerDataSection = dataSection.getConfigurationSection("玩家数据");
                Map<String, Object> map = new HashMap<>();
                for ( String playerName : playerDataSection.getKeys(false)) {
                    ConfigurationSection playerSection = playerDataSection.getConfigurationSection(playerName);
                    for ( String attribute : playerSection.getKeys(false) ) {
                        switch (attribute) {
                            case "玩家已出次数":
                            case "玩家已保底次数":
                            case "玩家已出保底次数":
                                map.put(attribute, playerSection.getInt(attribute));
                                break;
                        }
                    }
                    playerDataMap.put(playerName, map);
                }
            }
        }

        initializePlayerData();
    }

    private void initializePlayerData() {
        if (player != null) {
            ConfigurationSection section = dataConfig.getConfigurationSection("奖品数据."+reward.getRewardName()+".玩家数据." + player.getName());
            if (section == null) {
                section = dataConfig.createSection("玩家数据." + player.getName());
                section.set("玩家已出次数", 0);
                section.set("玩家已保底次数", 0);
                section.set("玩家已出保底次数", 0);
            }
            try {
                dataConfig.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initializePlayerData(Player player) {
        this.player = player;
        checkIfEnable();
        initializePlayerData();
        getDisplayItemStack();
    }

    public LotteryReward getReward() {
        return reward;
    }

    private void checkIfEnable() {
        if ( !requirementList.isEmpty()) {
            int index = 0;
            for ( String requirement : requirementList) {
                String[] spilted = requirement.split(" ");
                switch (spilted[0]) {
                    case "全服已抽物品次数":
                        String rewardName = spilted[1];
                        int times = Integer.parseInt(spilted[2]);
                        if ( totaltime < times ) {
                            isEnabled = false;
                        }
                        serverRequirementLength++;
                        break;
                    case "全服已抽次数":
                        int time = Integer.parseInt(spilted[1]);
                        if (spilted.length == 2) {
                            requirementList.set(index, requirement+" "+poolName);
                        }
                        if ( poolTotalTime < time ) {
                            isEnabled = false;
                        }
                        serverRequirementLength++;
                        break;
                    case "玩家已抽物品次数":
                        String rewardName2 = spilted[1];
                        int time2 = Integer.parseInt(spilted[2]);
                        if ( (int)playerDataMap.get(player.getName()).get("已出次数") < time2) {
                            isEnabled = false;
                        }
                        break;
                    case "玩家已抽次数":
                        int time3 = Integer.parseInt(spilted[1]);
                        if (spilted.length == 2) {
                            requirementList.set(index, requirement+" "+poolName);
                        }
                        if ( poolPlayerTotalTime.get(player.getName()) < time3) {
                            isEnabled = false;
                        }
                        break;
                }
                index++;
            }
        }

        if ( configMap.containsKey("全服限制数量")) {
            int limit = (int)configMap.get("全服限制数量");
            if ( limit - totaltime <= 0) isEnabled = false;
        }
        if (configMap.containsKey("玩家限制数量")) {
            int limit = (int) configMap.get("玩家限制数量");
            int playerTime = (int)playerDataMap.get(player.getName()).get("玩家已出次数");
            if (limit - playerTime <= 0) isEnabled = false;
        }
    }

    public String replace(LotteryData data, String lore) {
        boolean success;
        LotteryData originalData = data;


        if (lore.contains("#")) {
            Pattern pattern = Pattern.compile("(.*)#(.*)#(.*)");
            Matcher matcher = pattern.matcher(lore);
            matcher.find();
            String poolName = matcher.group(1);
            String rewardName = matcher.group(2);
            String attribute = matcher.group(3);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(Lottery.getInstance().getDataFolder(), "pools/"+poolName+".yml"));

            data = new LotteryData(config, poolName, rewardName, this.player);
        }
        lore = replace2(lore, data);
        return lore;
    }

    private String replace2(String expression, LotteryData data) {
        expression = ExtraUtils.replaceAll(expression, "真实概率", String.valueOf(data.configMap.get("真实概率")));
        expression = ExtraUtils.replaceAll(expression, "显示概率", String.valueOf(data.configMap.get("显示概率")));
        expression = ExtraUtils.replaceAll(expression, "全服限制数量", String.valueOf(data.configMap.get("全服限制数量")));
        expression = ExtraUtils.replaceAll(expression, "玩家限制数量", String.valueOf(data.configMap.get("玩家限制数量")));
        expression = ExtraUtils.replaceAll(expression, "玩家保底次数", String.valueOf(data.playerDataMap.get(data.player.getName()).get("玩家已保底次数")));
        expression = ExtraUtils.replaceAll(expression, "玩家已抽奖池次数", String.valueOf(data.poolPlayerTotalTime.get(data.player.getName())));
        expression = ExtraUtils.replaceAll(expression, "玩家已出奖品次数", String.valueOf(data.playerDataMap.get(data.player.getName()).get("玩家已出次数")));
        if (expression.contains("全服限制开启条件")) {
            if (expression.contains("全服已抽物品次数")) {
                if (expression.contains("奖品名")) {
                    String[] spilted = expression.split("-");
                    if (spilted.length == 3) {
                        expression = ExtraUtils.replaceAll(expression, "全服限制开启条件-全服已抽物品次数-奖品名", data.requirementList.get(0).split(" ")[1]);
                    } else if (spilted.length == 4) {
                        int index2 = Integer.parseInt(spilted[3]);
                        expression = ExtraUtils.replaceAll(expression, "全服限制开启条件-全服已抽物品次数-奖品名-\\d+", data.requirementList.get(index2).split(" ")[1]);
                    }
                } else {
                    String[] spilted = expression.split("-");
                    if (spilted.length == 3) {
                        expression = ExtraUtils.replaceAll(expression, "全服限制开启条件-全服已抽物品次数-次数", String.valueOf(data.requirementList.get(0).split(" ")[2]));
                    } else if (spilted.length == 4) {
                        int index2 = Integer.parseInt(spilted[3]);
                        expression = ExtraUtils.replaceAll(expression, "全服限制开启条件-全服已抽物品次数-次数-\\d+", String.valueOf(data.requirementList.get(index2).split(" ")[2]));
                    }
                }
            }
            if (expression.contains("全服已抽总次数")) {
                if (expression.contains("奖池名")) {
                    String[] spilted = expression.split("-");
                    if (spilted.length == 3) {
                        expression = ExtraUtils.replaceAll(expression, "全服限制开启条件-全服已抽总次数-奖池名", data.requirementList.get(0).split(" ")[1]);
                    } else if (spilted.length == 4) {
                        int index2 = Integer.parseInt(spilted[3]);
                        expression = ExtraUtils.replaceAll(expression, "全服限制开启条件-全服已抽总次数-奖池名-\\d+", data.requirementList.get(index2).split(" ")[1]);
                    }
                } else {
                    String[] spilted = expression.split("-");
                    if (spilted.length == 3) {
                        expression = ExtraUtils.replaceAll(expression, "全服限制开启条件-全服已抽总次数-次数", String.valueOf(data.requirementList.get(0).split(" ")[2]));
                    } else if (spilted.length == 4) {
                        int index2 = Integer.parseInt(spilted[3]);
                        expression = ExtraUtils.replaceAll(expression, "全服限制开启条件-全服已抽总次数-次数-\\d+", String.valueOf(data.requirementList.get(index2).split(" ")[2]));
                    }
                }
            }
        }

        if (expression.contains("玩家限制开启条件")) {
            if (expression.contains("玩家已抽物品次数")) {
                String[] spilted = expression.split("-");
                if (expression.contains("奖品名")) {
                    if (spilted.length == 3) {
                        expression = ExtraUtils.replaceAll(expression, "玩家限制开启条件-玩家已抽物品次数-奖品名", data.requirementList.get(serverRequirementLength).split(" ")[1]);
                    } else if (spilted.length == 4) {
                        int index2 = Integer.parseInt(spilted[3]);
                        expression = ExtraUtils.replaceAll(expression, "玩家限制开启条件-玩家已抽物品次数-奖品名-\\d+", data.requirementList.get(serverRequirementLength + index2).split(" ")[1]);
                    }
                } else {
                    if (spilted.length == 3) {
                        expression = ExtraUtils.replaceAll(expression, "玩家限制开启条件-玩家已抽物品次数-次数", String.valueOf(data.requirementList.get(serverRequirementLength).split(" ")[2]));
                    } else if (spilted.length == 4) {
                        int index2 = Integer.parseInt(spilted[3]);
                        expression = ExtraUtils.replaceAll(expression, "玩家限制开启条件-玩家已抽物品次数-次数-\\d+", String.valueOf(data.requirementList.get(serverRequirementLength + index2).split(" ")[2]));
                    }
                }
            }
            if (expression.contains("玩家已抽总次数")) {
                if (expression.contains("奖池名")) {
                    String[] spilted = expression.split("-");
                    if (spilted.length == 3) {
                        expression = ExtraUtils.replaceAll(expression, "玩家限制开启条件-玩家已抽总次数-奖池名", data.requirementList.get(serverRequirementLength).split(" ")[1]);
                    } else if (spilted.length == 4) {
                        int index2 = Integer.parseInt(spilted[3]);
                        expression = ExtraUtils.replaceAll(expression, "玩家限制开启条件-玩家已抽总次数-奖池名-\\d+", data.requirementList.get(serverRequirementLength + index2).split(" ")[1]);
                    }
                } else {
                    String[] spilted = expression.split("-");
                    if (spilted.length == 3) {
                        expression = ExtraUtils.replaceAll(expression, "玩家限制开启条件-玩家已抽总次数-次数", String.valueOf(data.requirementList.get(serverRequirementLength).split(" ")[2]));
                    } else if (spilted.length == 4) {
                        int index2 = Integer.parseInt(spilted[3]);
                        expression = ExtraUtils.replaceAll(expression, "玩家限制开启条件-玩家已抽总次数-次数-\\d+", String.valueOf(data.requirementList.get(serverRequirementLength + index2).split(" ")[2]));
                    }
                }
            }
        }
        expression = ExtraUtils.replaceAll(expression, "全服已抽此奖池次数", String.valueOf(data.poolTotalTime));
        expression = ExtraUtils.replaceAll(expression, "玩家已抽此奖池次数", String.valueOf(data.poolPlayerTotalTime.get(data.player.getName())));
        expression = ExtraUtils.replaceAll(expression, "全服已出此奖品次数", String.valueOf(data.totaltime));
        expression = ExtraUtils.replaceAll(expression, "玩家已出此奖品次数", String.valueOf(data.playerDataMap.get(data.player.getName()).get("玩家已出次数")));
        expression = ExtraUtils.replaceAll(expression, "玩家此奖品保底次数", String.valueOf(data.playerDataMap.get(data.player.getName()).get("玩家已保底次数")));
        expression = ExtraUtils.replaceAll(expression, "玩家已出此奖品保底次数", String.valueOf(data.playerDataMap.get(data.player.getName()).get("玩家已出保底次数")));

        return expression;
    }

    public ItemStack getDisplayItemStack() {
        if (displayItemstack == null) {
            ItemStack itemStack = reward.getDisplayItem();
            LotteryData data = this;
            if (!customLores.isEmpty()) {
                int index = 0;
                for (String lore : customLores) {
                    lore = TextUtil.convertColor(ExtraUtils.processFormattedVariable(data, lore));
                    customLores.set(index, lore);
                    index++;
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lores = itemMeta.getLore();
                lores.addAll(customLores);
                itemMeta.setLore(lores);
                itemStack.setItemMeta(itemMeta);
            }
            if (!isEnabled) {
                itemStack.setType(Material.BARRIER);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(itemMeta.getDisplayName() + TextUtil.convertColor("&c&l不可用"));
                itemStack.setItemMeta(itemMeta);
            }
            displayItemstack = itemStack;
            return displayItemstack;
        } else {
            return displayItemstack;
        }
    }

    public void preProcess2() {
        if (isEnabled) {
            if (configMap.containsKey("保底次数")) {
                int configTime = (int) configMap.get("保底次数");
                int playerTime = (int) playerDataMap.get(player.getName()).get("玩家已保底次数");
                if (playerTime < configTime) {
                    Map<String, Object> map1 = playerDataMap.get(player.getName());
                    map1.put("玩家已保底次数", playerTime + 1);
                    playerDataMap.put(player.getName(), map1);
                } else {
                    Map<String, Object> map1 = playerDataMap.get(player.getName());
                    map1.put("玩家已出次数", (int) map1.get("玩家已出次数") + 1);
                    map1.put("玩家已保底次数", 0);
                    map1.put("玩家已出保底次数", (int) map1.get("玩家已保底次数") + 1);
                    int floorLimit = configMap.get("保底限量") == null ? 0 : (int) configMap.get("保底限量");
                    if (floorLimit != 0 && floorLimit != -1 && (int)map1.get("玩家已出保底次数") >= floorLimit) {
                        isEnabled = false;
                    }
                    GlobalConfig.floorList.put(player.getName(), reward);
                    playerDataMap.put(player.getName(), map1);
                }
                serializeToYmlFile();
            }
        }
    }

    public void preProcess(String poolName, Player player) {
        ConfigurationSection section = dataConfig.getConfigurationSection("奖品数据");
        for ( String rewardName : section.getKeys(false)) {
            ConfigurationSection playerSection = section.getConfigurationSection(rewardName+".玩家数据."+player.getName());
            int configTime = poolConfig.getInt("奖品列表."+rewardName+".保底次数");
            int configLimit = poolConfig.getInt("奖品列表."+rewardName+".保底限量");
            if ( configTime != 0 && configTime != -1) {
                int playerTime = playerSection.getInt("玩家已保底次数");
                int playerLimit = playerSection.getInt("玩家已出保底次数");
                int playerTime2 = playerSection.getInt("玩家已出次数");
                playerTime++;
                if ( playerTime >= configTime ) {
                    playerLimit++;
                    if (configLimit != 0 && configLimit != -1 && playerLimit > configLimit) {
                    } else {
                        playerTime = 0;
                        LotteryReward reward = new LotteryReward(rewardName);
                        GlobalConfig.floorList.put(player.getName(), reward);
                        playerTime2++;
                        playerSection.set("玩家已出保底数量", playerLimit);
                        playerSection.set("玩家已出次数", playerTime2);
                    }
                }
                playerSection.set("玩家已保底次数", playerTime);
            }
            try {
                poolConfig.save(new File("pools/"+poolName+".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            dataConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void serializeToYmlFile() {
        for ( String playerName : playerDataMap.keySet() ) {
            for ( String key : playerDataMap.get(playerName).keySet() ) {
                String node = playerName+"."+key;
                dataConfig.set(node, playerDataMap.get(key));
            }
        }
        try {
            dataConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(Player player) {
        reward.execute(player);
    }

    private void refreshFile(String poolname, File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        LotteryPool pool = new LotteryPool(poolname, false);

    }

    public void refreshPlayerData(String playerName) {
        playerDataMap.get(playerName).forEach((key, value) -> {
            if (!key.equals("玩家已出次数")) {
               playerDataMap.get(playerName).put(key, 0);
            }
        });
        serializeToYmlFile();
    }

    public void refreshGlobalData() {
        totaltime = 0;
        dataConfig.set("奖品数据."+reward.getRewardName()+".全服已出此奖品次数", 0);
        try {
            dataConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
