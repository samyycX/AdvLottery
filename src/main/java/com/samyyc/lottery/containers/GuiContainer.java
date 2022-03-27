package com.samyyc.lottery.containers;

import com.samyyc.lottery.objects.LotteryGUI;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiContainer {

    private static final Map<UUID, LotteryGUI> GUI_MAP = new HashMap<>();

    public static LotteryGUI getGUI(UUID playerUUID) {
        return GUI_MAP.get(playerUUID);
    }

    public static void addGUI(UUID playerUUID, LotteryGUI gui) {
        GUI_MAP.put(playerUUID, gui);
    }

    public static void removeGUI(UUID playerUUID) {
        GUI_MAP.remove(playerUUID);
    }

    public static void destroy() {
        GUI_MAP.clear();
    }

}
