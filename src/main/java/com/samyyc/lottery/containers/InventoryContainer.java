package com.samyyc.lottery.containers;

import com.samyyc.lottery.objects.LotteryInventory;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryContainer {

    private static final Map<UUID, LotteryInventory> INVENTORY_MAP = new HashMap<>();

    public static LotteryInventory getInventory(UUID uuid) {
        if (INVENTORY_MAP.get(uuid) == null) {
            addInventory(uuid);
        }
        return INVENTORY_MAP.get(uuid);
    }

    public static void addInventory(UUID uuid) {
        LotteryInventory inventory = new LotteryInventory(Bukkit.getPlayer(uuid));
        INVENTORY_MAP.put(uuid, inventory);
    }

    public static void destroy() {
        INVENTORY_MAP.clear();
    }

}
