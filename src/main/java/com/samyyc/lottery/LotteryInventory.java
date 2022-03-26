package com.samyyc.lottery;

import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LotteryInventory {

    private Player player;
    private Map<Integer, Inventory> inventoryMap;
    private boolean isEnabled = true;
    private int maxItemLimit = 0;
    private YamlConfiguration config;
    private File file;
    private List<String> keyList;

    public LotteryInventory(Player player) {
        this.player = player;
        this.isEnabled = Lottery.getInstance().getConfig().getBoolean("启用抽奖背包");
        if (isEnabled) {
            file = new File(Lottery.getInstance().getDataFolder(), "背包/"+ player.getName()+".yml");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            config = YamlConfiguration.loadConfiguration(file);
            maxItemLimit = Lottery.getInstance().getConfig().getInt("抽奖背包限制");
            keyList = config.getStringList("背包");
            initInventory();
        }
    }

    public void initInventory() {
        inventoryMap = new LinkedHashMap<>();
        if (keyList.size() <= 54) {
            inventoryMap.put(0, Bukkit.createInventory(null, 54, TextUtil.convertColor("&b抽奖背包 - &e"+player.getName()+" - &a第1页")));
            for ( int i = 0; i < keyList.size(); i++) {
                String rewardName = keyList.get(i);
                LotteryReward reward = new LotteryReward(rewardName);
                Inventory inventory = inventoryMap.get(0);
                inventory.setItem(i, reward.getDisplayItem());
                inventoryMap.put(0, inventory);
            }
        } else {
            int pageCount = keyList.size() / 54 + 1;
            for (int i = 0; i < pageCount; i++) {
                Inventory inventory = Bukkit.createInventory(null, 54, TextUtil.convertColor("&b抽奖背包 - &e" + player.getName() + " - &a第" + (i + 1) + "页"));
                for (int j = 0; j < 54 - 9; j++) {
                    try {
                        String rewardName = keyList.get(j + (54 - 9) * i);
                        LotteryReward reward = new LotteryReward(rewardName);
                        inventory.setItem(j, reward.getDisplayItem());
                    } catch (IndexOutOfBoundsException e) {

                    }
                }
                ItemStack decoration = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3);
                ItemMeta meta = decoration.getItemMeta();
                meta.setDisplayName(TextUtil.convertColor("&e装饰"));
                decoration.setItemMeta(meta);

                ItemStack previousPage = new ItemStack(Material.WOOL, 1, (short) 14);
                meta = previousPage.getItemMeta();
                meta.setDisplayName(TextUtil.convertColor("&c上一页"));
                previousPage.setItemMeta(meta);

                ItemStack nextPage = new ItemStack(Material.WOOL, 1, (short) 5);
                meta = nextPage.getItemMeta();
                meta.setDisplayName(TextUtil.convertColor("&a下一页"));
                nextPage.setItemMeta(meta);

                // 45, 46, 47, 48, 49, 50, 51, 52, 53
                for (int temp = 45; temp < 54; temp++) {
                    inventory.setItem(temp, decoration);
                }
                if (i == 0) {
                    inventory.setItem(49, nextPage);
                } else if (i == pageCount - 1) {
                    inventory.setItem(49, previousPage);
                } else {
                    inventory.setItem(48, previousPage);
                    inventory.setItem(50, nextPage);
                }
                inventoryMap.put(i, inventory);
            }
        }
    }


    public Inventory getInventory(int page) {
        return inventoryMap.get(page);
    }
    public void addReward(LotteryReward reward) {
        if (maxItemLimit == 0 || maxItemLimit == -1 || keyList.size() + 1 <= maxItemLimit) {
            keyList.add(reward.getRewardName());
            config.set("背包", keyList);
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            initInventory();
        }
    }
    public void removeReward(int slot, int page) {
        keyList.remove(slot);
        config.set("背包", keyList);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.openInventory(getInventory(page));
        initInventory();
    }

    public void processClickedSlot(int slot, int page) {
        page-=1;
        ItemStack clickedItem = getInventory(page).getItem(slot);
        if (clickedItem == null) return;
        if (clickedItem.getType().equals(Material.AIR)) return;
        if (clickedItem.getItemMeta().getDisplayName().equals(TextUtil.convertColor("&e装饰"))) return;
        if (clickedItem.getItemMeta().getDisplayName().equals(TextUtil.convertColor("&c上一页"))) {
            player.openInventory(getInventory(page - 1));
        } else if (clickedItem.getItemMeta().getDisplayName().equals(TextUtil.convertColor("&a下一页"))) {
            player.openInventory(getInventory(page + 1));
        } else {
            slot = slot + (54-9) * page;
            LotteryReward reward = new LotteryReward(keyList.get(slot));
            reward.execute(player);
            removeReward(slot, page);
        }
    }
}
