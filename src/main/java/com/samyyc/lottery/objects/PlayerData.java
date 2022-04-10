package com.samyyc.lottery.objects;

import com.samyyc.lottery.configs.GlobalConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PlayerData {

    private Map<String, Integer> dataMap = new HashMap<>();
    private ConfigurationSection section;

    public PlayerData(ConfigurationSection section) {
        this.section = section;
        for ( String key : section.getKeys(false) ) {
            dataMap.put(key, section.getInt(key));
        }
        if (!section.getKeys(false).contains("玩家已出次数")) {
            init();
        }
    }


    public Map<String, Integer> getDataMap() {
        return dataMap;
    }

    public String getAttribute(String attribute) {
        for (Map.Entry<String, Integer> entry : dataMap.entrySet() ) {
            if (entry.getKey().equalsIgnoreCase(attribute)) {
                return String.valueOf(entry.getValue());
            }
        }
        return null;
    }

    // 上级对象运行此方法之后，应该保存配置文件
    public void refreshPlayerData() {
        section.set("玩家已出保底次数", 0);
    }

    public static void init(ConfigurationSection section) {
        section.set("玩家已出次数", 0);
        section.set("玩家已保底次数", 0);
        section.set("玩家已出保底次数", 0);
    }

    public void init() {
        init(this.section);
    }

    public void addTime() {
        addData("玩家已出次数");
        addFloorTime();
    }

    public void addFloorTime() {
        addData("玩家已保底次数");
    }

    public void addFlooredTime() {
        addData("玩家已出保底次数");
    }

    public void addData(String key) {
        int time = dataMap.get(key);
        dataMap.put(key, ++time);
        section.set(key, time);
    }

    public int getFloorTime() {
        return dataMap.get("玩家已保底次数");
    }

    public void resetFloorTime() {
        section.set("玩家已保底次数", 0);
    }

    public boolean check(int floorLimit) {
        if (floorLimit != 0 && floorLimit != -1) {
            return dataMap.get("玩家已保底次数") >= floorLimit;
        } else {
            return false;
        }
    }
}
