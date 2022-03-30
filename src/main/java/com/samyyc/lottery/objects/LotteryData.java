package com.samyyc.lottery.objects;

import com.samyyc.lottery.containers.RewardContainer;
import com.samyyc.lottery.utils.Calculator;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LotteryData {

    private String dataName;
    private ConfigurationSection poolSection;
    private ConfigurationSection dataSection;
    private LotteryReward reward;

    // 奖池配置部分
    private Map<String, Object> configMap = new HashMap<>();

    // 数据部分
    private int dataServerTime;
    private Map<String, PlayerData> dataPlayerDataMap = new HashMap<>();


    // itemstack
    private List<String> lores;
    private ItemStack displayItemStack;

    public LotteryData(String dataName, ConfigurationSection poolSection, ConfigurationSection dataSection) {
        this.dataName = dataName;
        this.poolSection = poolSection;
        this.dataSection = dataSection;
        this.reward = RewardContainer.getReward(dataName);
        readData();
    }

    public static void initPoolSection(ConfigurationSection section) {
        section.set("真实概率", 0);
        section.set("显示概率", "example");
        section.set("物品限量", 0);
        section.set("保底次数", 0);
        section.set("保底限量", 0);
        section.set("全服限制数量", 0);
        section.set("lores", new ArrayList<String>(){{add("test");}});
    }

    public void readData() {
        for (String key : poolSection.getKeys(false) ) {
            if (!key.equalsIgnoreCase("lores")) {
                if (key.equalsIgnoreCase("真实概率")) {
                    configMap.put(key, TextUtil.convertColor(String.valueOf(poolSection.get(key))));
                } else {
                    configMap.put(key, poolSection.get(key));
                }
            } else {
                lores = poolSection.getStringList(key);
            }
        }
        dataServerTime = dataSection.getInt("全服已出此奖品次数");
        ConfigurationSection section = dataSection.getConfigurationSection("玩家数据");
        for (String playerName : section.getKeys(false)) {
            ConfigurationSection playerSection = section.getConfigurationSection(playerName);
            PlayerData playerData = new PlayerData(playerSection);
            dataPlayerDataMap.put(playerName, playerData);
        }
    }

    private String replaceData(String text) {
        for (Map.Entry<String, Object> entry : configMap.entrySet() ) {
            text = text.replace("{"+entry.getKey()+"}", String.valueOf(entry.getValue()));
        }
        for (Map.Entry<String, PlayerData> entry : dataPlayerDataMap.entrySet() ) {
            for ( Map.Entry<String, Object> entry1 : entry.getValue().getDataMap().entrySet() ) {
                text = text.replace("{"+entry.getKey()+"}", String.valueOf(entry1.getValue()));
            }
        }
        Pattern pattern = Pattern.compile("~.*~");
        Matcher matcher = pattern.matcher(text);
        matcher.find();
        int matcher_start = 0;
        while (matcher.find(matcher_start)) {
            String s = matcher.group();
            double result = Calculator.executeExpression(s.replaceAll("~",""));
            text = text.replace(s, String.valueOf(result));
        }
        return text;
    }

    public String getAttribute(String attribute, Player player) {
        for (Map.Entry<String, Object> entry : configMap.entrySet() ) {
            if ( attribute.equalsIgnoreCase(entry.getKey())) {
                return String.valueOf(entry.getValue());
            }
        }
        return dataPlayerDataMap.get(player.getName()).getAttribute(attribute);
    }

    public ItemStack getDisplayItemStack() {
        if (displayItemStack == null) {
            displayItemStack = reward.getDisplayItem();
            if (lores != null) {
                for (int i = 0; i < lores.size(); i++) {
                    String s = lores.get(i);
                    s = replaceData(s);
                    lores.set(i, s);
                }
                ItemMeta im = displayItemStack.getItemMeta();
                if (im.hasLore()) {
                    List<String> lore = im.getLore();
                    lore.addAll(lores);
                    im.setLore(lore);
                } else {
                    im.setLore(lores);
                }
                displayItemStack.setItemMeta(im);
            }
        }
        return displayItemStack;
    }

    public boolean invalidFilter(Player player) {

        for ( String key : poolSection.getKeys(false) ) {
            if (key.equalsIgnoreCase("全服限制数量")) {
                int time = poolSection.getInt(key);
                if (dataServerTime > time) {
                    return false;
                }
            }
            if (key.equalsIgnoreCase("玩家限制数量")) {
                Object a = dataPlayerDataMap.get(player.getName()).getAttribute("玩家已出次数");
                if (a != null) {
                    int time = (int) a;
                    int limit = poolSection.getInt(key);
                    if (time > limit) {
                        return false;
                    }
                } else {
                    dataPlayerDataMap.get(player.getName()).init();
                }
            }
            if (key.equalsIgnoreCase("保底限量")) {
                Object a = dataPlayerDataMap.get(player.getName()).getAttribute("玩家已出保底次数");
                if (a != null) {
                    int time = (int) a;
                    int limit = poolSection.getInt(key);
                    if (time > limit) {
                        return false;
                    }
                } else {
                    dataPlayerDataMap.get(player.getName()).init();
                }
            }
        }
        return true;
    }

    public void refreshGlobalData() {
        dataServerTime = 0;
        dataSection.set("全服已出此奖品次数", 0);
    }

    public void refreshPlayerData(Player player) {
        dataPlayerDataMap.get(player.getName()).refreshPlayerData();
    }

    public int getRealProbability() {
        return (int) configMap.get("真实概率");
    }

    public LotteryReward getReward() {
        return reward;
    }
}
