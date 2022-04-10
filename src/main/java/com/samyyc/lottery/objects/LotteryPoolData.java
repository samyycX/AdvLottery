package com.samyyc.lottery.objects;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.enums.Message;
import com.samyyc.lottery.objects.group.LotteryDataGroup;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LotteryPoolData {

    // 奖池里的配置
    private ConfigurationSection poolConfig;
    // 数据文件里的数据
    private YamlConfiguration dataConfig;
    private File dataFile;
    // 不能被直接调用， 需要分玩家给出过滤后的对象
    private Map<String, LotteryData> dataMap = new HashMap<>();

    // 公共数据
    private int globalTotalTime;
    private Map<String, Integer> playerTotalTimeMap = new HashMap<>();

    public LotteryPoolData(String poolName, YamlConfiguration poolConfig) {
        this.poolConfig = poolConfig.getConfigurationSection("奖品列表");
        File file = new File(Lottery.getInstance().getDataFolder(), "数据\\"+poolName+".yml");
        if (!file.exists()) {
            initializeNewDataFile(file);
        }
        this.dataFile = file;
        dataConfig = YamlConfiguration.loadConfiguration(file);

        // 下面的操作不可能导致空指针！
        globalTotalTime = dataConfig.getInt("全服已抽此奖池次数");
        ConfigurationSection playerTotalTimeSection = dataConfig.getConfigurationSection("玩家已抽此奖池次数");
        for (String key : playerTotalTimeSection.getKeys(false)) {
            playerTotalTimeMap.put(key, playerTotalTimeSection.getInt(key));
        }

        ConfigurationSection rewardSection = poolConfig.getConfigurationSection("奖品列表");
        if (rewardSection == null) return;
        for ( String rewardName : rewardSection.getKeys(false)) {
            ConfigurationSection poolSection = rewardSection.getConfigurationSection(rewardName);
            ConfigurationSection dataSection = dataConfig.getConfigurationSection("奖品数据."+rewardName);
            if (dataSection == null) {
                dataSection = dataConfig.createSection("奖品数据."+rewardName);
                save();
            }

            LotteryData data;

            if (!rewardName.startsWith("奖品组#")) {
                data = new LotteryData(rewardName, poolSection, dataSection);

            } else {
                data = new LotteryDataGroup(rewardName, poolSection, dataSection);

            }
            data.giveConfig(dataConfig, file);
            dataMap.put(rewardName, data);
        }
    }


    public LotteryData roll(Map<String, LotteryData> dataMap) {

        Random random = new Random();
        int sum = 0;
        for ( LotteryData data : dataMap.values() ) {
            sum+=data.getRealProbability();
        }
        int rand = random.nextInt(sum)+1;
        for (LotteryData data : dataMap.values() ) {
            rand -= data.getRealProbability();
            if (rand<=0) {
                if (data instanceof LotteryDataGroup) {
                    ((LotteryDataGroup) data).roll();
                }
                return data;
            }
        }
        return null;
    }

    public String getAttribute(String attribute, String rewardName, Player player) {
        if (attribute.equalsIgnoreCase("全服已抽此奖池次数")) {
            return String.valueOf(globalTotalTime);
        }
        if (attribute.equalsIgnoreCase("玩家已抽此奖池次数")) {
            return String.valueOf(playerTotalTimeMap.get(player.getName()));
        }
        return dataMap.get(rewardName).getAttribute(attribute, player);
    }

    private File initializeNewDataFile(File file) {
        try {
            file.createNewFile();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("全服已抽此奖池次数", 0);
            config.createSection("玩家已抽此奖池次数");
            for (String key : poolConfig.getKeys(false) ) {
                config.set("奖品数据."+key+".全服已出此奖品次数", 0);
                config.createSection("奖品数据."+key);
                config.createSection("奖品数据."+key+".玩家数据");
            }
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().info(Message.ERROR_FILE.getMessage());
        }
        return file;
    }

    public Map<String, LotteryData> invalidFilter(Player player) {
        Map<String, LotteryData> mapForPlayer = new HashMap<>(dataMap);
        mapForPlayer.entrySet().removeIf(entry -> !entry.getValue().invalidFilter(player));
        return mapForPlayer;
    }

    public void refreshGlobalData() {
        dataMap.values().forEach(LotteryData::refreshGlobalData);
        save();
    }

    public void refreshPlayerData(Player player) {
        dataMap.values().forEach(data -> {
            data.refreshPlayerData(player);
        });
        save();
    }

    public LotteryData getDataByItemstack(ItemStack itemStack) {
        System.out.println(itemStack);
        if (itemStack == null) return null;
        if (itemStack.getItemMeta().getLore().get(itemStack.getItemMeta().getLore().size()-1).startsWith("§a属于奖品组: ")) {
            String groupName = "奖品组#"+itemStack.getItemMeta().getLore().get(itemStack.getItemMeta().getLore().size()-1).replace("§a属于奖品组: ", "");
            System.out.println(groupName);
            System.out.println(dataMap);
            if (dataMap.get(groupName) != null) {
                LotteryDataGroup group = (LotteryDataGroup) dataMap.get(groupName);
                System.out.println(group.getDataByItemStack(itemStack));
                return group.getDataByItemStack(itemStack);
            }
        }
        for ( LotteryData data : dataMap.values() ) {
           if (!(data instanceof LotteryDataGroup)) {
               if (data.getDisplayItemStack().getType().equals(itemStack.getType())
                       &&
                       data.getDisplayItemStack().getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName())
                       &&
                       data.getDisplayItemStack().getAmount() == itemStack.getAmount()
               )   {
                   return data;
               }
           }
        }
        return null;
    }

    public void initReward(String rewardName) {
        ConfigurationSection poolSection = poolConfig.getConfigurationSection(rewardName);
        ConfigurationSection dataSection = dataConfig.getConfigurationSection("奖品数据."+rewardName);
        if (poolSection == null) {
            poolSection = poolConfig.createSection(rewardName);
            LotteryData.initPoolSection(poolSection);
            save();
        }
        if (dataSection == null) {
            dataSection = LotteryData.initDataSection(dataConfig.createSection("奖品数据."+rewardName));
            save();
        }
        LotteryData data = new LotteryData(rewardName, poolSection, dataSection);
        data.giveConfig(dataConfig, dataFile);
        dataMap.put(rewardName, data);
    }

    public void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LotteryData getReward(String rewardName) {
        return dataMap.get(rewardName);
    }


    public void increaseRollTime(Player player) {
        globalTotalTime++;
        dataConfig.set("全服已抽此奖池次数", globalTotalTime);
        if (playerTotalTimeMap.get(player.getName()) == null) {
            dataConfig.getConfigurationSection("玩家已抽此奖池次数").set(player.getName(), 0);
            playerTotalTimeMap.put(player.getName(), 0);
        }
        int playerTime = playerTotalTimeMap.get(player.getName());
        playerTotalTimeMap.put(player.getName(), ++playerTime);
        dataConfig.getConfigurationSection("玩家已抽此奖池次数").set(player.getName(), playerTime);
        save();
    }
}
