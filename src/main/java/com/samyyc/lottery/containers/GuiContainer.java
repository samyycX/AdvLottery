package com.samyyc.lottery.containers;

import com.samyyc.lottery.objects.LotteryGUI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiContainer {

    private static final Map<UUID, LotteryGUI> GUI_MAP = new HashMap<>();

    public static LotteryGUI get(UUID playerUUID) {
        return GUI_MAP.get(playerUUID);
    }

    public static void add(UUID playerUUID, LotteryGUI gui) {
        GUI_MAP.put(playerUUID, gui);
    }

    public static void remove(UUID playerUUID) {
        GUI_MAP.remove(playerUUID);
    }

    public static void destroy() {
        GUI_MAP.clear();
    }

}
