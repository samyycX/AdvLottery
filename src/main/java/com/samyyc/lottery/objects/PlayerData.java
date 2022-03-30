package com.samyyc.lottery.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PlayerData {

    private Map<String, Object> dataMap = new HashMap<>();
    private ConfigurationSection section;

    public PlayerData(ConfigurationSection section) {
        this.section = section;
        for ( String key : section.getKeys(false) ) {
            dataMap.put(key, section.get(key));
        }
    }


    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public String getAttribute(String attribute) {
        for (Map.Entry<String, Object> entry : dataMap.entrySet() ) {
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

    public void init() {
        section.set("玩家已出次数", 0);
        section.set("玩家已保底次数", 0);
        section.set("玩家已出保底次数", 0);
    }
}
