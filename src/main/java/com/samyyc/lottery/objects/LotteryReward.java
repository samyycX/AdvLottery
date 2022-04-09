package com.samyyc.lottery.objects;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.apis.APIContainer;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.utils.APIUtils;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * 奖励物品
 *
 */
public class LotteryReward {

    private String rewardName;

    private File file;

    private YamlConfiguration config;

    private Map<String, ItemStack> rewardItemStackMap = new LinkedHashMap<>();

    private ItemStack GUIDisplayItemstack;

    /**
     * 构造器
     * @param rewardName 奖励名称
     */
    public LotteryReward(String rewardName) {
        this.rewardName = rewardName;
        // 初始化配置文件
        file = new File(Lottery.getInstance().getDataFolder(), "奖品/"+rewardName+".yml");
        if (!file.exists()) {
            try {
                File template = new File(Lottery.getInstance().getDataFolder(), "奖品/example.yml");
                file.createNewFile();
                FileUtil.copy(template, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        // 初始化奖品

        // 初始化奖品的ItemStack
        // 这一部分分两种，一种手动配置物品属性，一种自动配置物品属性
        // 手动代表yml里设置，自动代表游戏内通过指令自动获取
        ConfigurationSection ItemStackListSection = config.getConfigurationSection("物品预处理列表");
        if (!Objects.isNull(ItemStackListSection)) {
            Set<String> set = ItemStackListSection.getKeys(false);
            for ( String key : set ) {
                ConfigurationSection itemSection = ItemStackListSection.getConfigurationSection(key);
                rewardItemStackMap.put(key, ExtraUtils.generateItemstackFromYml(itemSection));
            }
        }

        // 初始化GUI显示物品
        // 这一部分分两种，一种手动配置物品属性，一种自动配置物品属性
        // 手动代表yml里设置，自动代表游戏内通过指令自动获取
        // 如果用户没有设定这一部分，则自动采取物品预处理列表中第一个itemstack
        // 如果物品预处理列表为空，则报错
        ConfigurationSection GUISection = config.getConfigurationSection("gui显示物品");
        if ( !Objects.isNull(GUISection)) {
            GUIDisplayItemstack = ExtraUtils.generateItemstackFromYml(GUISection);

        } else {
            if ( !rewardItemStackMap.isEmpty() ) {
                GUIDisplayItemstack = rewardItemStackMap.entrySet().iterator().next().getValue();
            }
        }



    }

    public ItemStack getDisplayItem() {
        return GUIDisplayItemstack;
    }

    public String getRewardName() {
        return rewardName;
    }

    /**
     * 在YML内设置GUI显示物品
     * @param itemStack gui显示物品
     */
    public void setDisplayItem(ItemStack itemStack) {
        config.set("gui显示物品.自动配置", itemStack);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在YML内设置物品
     * @param itemName 物品在配置内的名称(不是itemstack的自定义名称!)
     * @param itemStack 物品
     */
    public void setItem(String itemName, ItemStack itemStack) {
        config.set("物品预处理列表."+itemName+".自动配置", itemStack);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void preExecute(Player player) {
        List<String> taskList = config.getStringList("奖励");
        for ( String command : taskList ) {
            String[] split = command.split(" ", 2);
            switch (split[0].toLowerCase()) {
                case "玩家运行指令":
                    player.performCommand(a(split[1]));
                    break;
                case "后台运行指令":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), a(split[1]));
                    break;
                case "玩家广播":
                    player.sendMessage(a(split[1]));
                    break;
                case "全服广播":
                    Bukkit.broadcastMessage(a(split[1]));
                    break;
                case "vault加钱":
                    if (APIUtils.isVaultEnabled) {
                        APIUtils.addPlayerVaultEconomy(player, Integer.parseInt(split[1]));
                    }
                    break;
                case "playerpoints加点券":
                    if (APIUtils.isPlayerPointsEnabled) {
                        APIUtils.addPlayerPoints(player, Integer.parseInt(split[1]));
                    }
                    break;
                default:
                    if (APIContainer.preRewardMap != null && APIContainer.preRewardMap.get(split[0]) != null) {
                        APIContainer.preRewardMap.get(split[0]).run(player, command);
                    }

            }
        }
    }
    /**
     * 执行奖励脚本
     * @param player 受奖励的玩家
     */
    public void execute(Player player) {
        List<String> taskList = config.getStringList("奖励");
        for (String command : taskList) {
            String[] split = command.split(" ");
            if (split[0].equals("给予物品")) {
                player.getInventory().addItem(rewardItemStackMap.get(split[1]));
            } else {
                if (APIContainer.rewardMap.containsKey(split[0])) {
                    APIContainer.rewardMap.get(split[0]).run(player, command);
                }
            }
        }
    }

    private String a(String b) {
        return TextUtil.convertColor(b.replaceAll("\\{player}",""));
    }

    public void resetLores() {
        ConfigurationSection GUISection = config.getConfigurationSection("gui显示物品");
        if ( !Objects.isNull(GUISection)) {
            GUIDisplayItemstack = ExtraUtils.generateItemstackFromYml(GUISection);

        } else {
            if ( !rewardItemStackMap.isEmpty() ) {
                GUIDisplayItemstack = rewardItemStackMap.entrySet().iterator().next().getValue();
            }
        }
    }


}
