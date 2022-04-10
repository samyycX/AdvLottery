package com.samyyc.lottery.runnables;

import com.samyyc.lottery.containers.GuiContainer;
import com.samyyc.lottery.objects.*;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.utils.FloorUtil;
import com.samyyc.lottery.utils.LogUtils;
import com.samyyc.lottery.enums.Message;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class ScriptRunnable implements Runnable {

    private String script;
    private Inventory inventory;
    private Player player;
    private LotteryPool pool;
    private Map<String, ItemStack> itemMap;
    private Map<String, LotteryData> dataMap;
    private List<Integer> floorSlots;

    public ScriptRunnable(String script, Player player, LotteryPool pool, Map<String, ItemStack> itemMap, Map<String, LotteryData> dataMap, List<Integer> floorSlots) {
        this.script = script;
        this.inventory = player.getOpenInventory().getTopInventory();
        this.player = player;
        this.pool = pool;
        this.itemMap = itemMap;
        this.dataMap = dataMap;
        this.floorSlots = floorSlots;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (script.startsWith("出奖")) {
                String[] split = script.split(" ");
                int targetSlot = Integer.parseInt(split[1]);
                LotteryGUI gui = GuiContainer.get(player.getUniqueId());
                LotteryData data;
                data = pool.getDataByItemstack(gui.inventory().getItem(targetSlot));
                System.out.println(data);
                data.preExecute(player, true);
                LotteryResult result = new LotteryResult(player, targetSlot, data.getReward());
                GlobalConfig.putResult(player.getUniqueId(), result);
                // TODO: 优化保底
                LogUtils.addLog(Message.LOG_ROLL_SUCCESS.getMessage()
                        .replace("{playername}",player.getName())
                        .replace("{poolname}", pool.getName())
                        .replace("{rewardname}",result.getLotteryReward().getRewardName()));

                floorSlots.forEach(slot -> {
                    if (FloorUtil.hasFloorData(player.getUniqueId())) {
                        LotteryData floorData = FloorUtil.getFloorData(player.getUniqueId());
                        inventory.setItem(slot, floorData.getDisplayItemStack());
                        floorData.preExecute(player, false);
                        LotteryResult result1 = new LotteryResult(player, slot, floorData.getReward());
                        GlobalConfig.putResult(player.getUniqueId(), result1);
                    }
                });
                player.openInventory(inventory);

            } else if (script.startsWith("轮换物品")) {
                String[] split = script.split(" ");
                int originSlot;
                int targetSlot; try {
                    originSlot = Integer.parseInt(split[1]);
                    targetSlot = Integer.parseInt(split[2]);
                } catch (NumberFormatException ignored) {
                    return;
                }
                //inventory.setItem(targetSlot, inventory.getItem(originSlot));
                LotteryGUI gui = GuiContainer.get(player.getUniqueId());
                gui.inventory().setItem(targetSlot, gui.inventory().getItem(originSlot));
                gui.showInventory(player);
                //player.getOpenInventory().getTopInventory().setItem(targetSlot, inventory.getItem(originSlot));
            } else if (script.startsWith("替换物品")) {
                String[] split = script.split(" ");
                int originSlot;
                try {
                    originSlot = Integer.parseInt(split[1]);
                } catch (NumberFormatException ignored) {
                    return;
                }
                ItemStack item = itemMap.get(split[2]);
                if (item != null) {
                    //inventory.setItem(originSlot, item);
                    LotteryGUI gui = GuiContainer.get(player.getUniqueId());
                    gui.inventory().setItem(originSlot, item);
                    gui.showInventory(player);
                    //player.getOpenInventory().getTopInventory().setItem(originSlot, item);
                }
            } else if (script.startsWith("播放音符")) {
                String[] split = script.split(" ");
                Instrument instrument;
                Note.Tone tone;
                try {
                    instrument = Instrument.valueOf(split[1]);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().info(Message.ERROR_UNKNOWN_INSTRUMENT.getMessage().replace("{insname}",split[1]));
                    return;
                }
                try {
                    tone = Note.Tone.valueOf(split[2]);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().info(Message.ERROR_UNKNOWN_TONE.getMessage().replace("{tonename}",split[2]));
                    return;
                }
                player.playNote(player.getLocation(), instrument, Note.natural(1, tone));
            } else if (script.startsWith("随机奖品")) {
                LotteryData data = pool.roll(dataMap);
                int slot = Integer.parseInt(script.split(" ")[1]);
                LotteryGUI gui = GuiContainer.get(player.getUniqueId());
                gui.inventory().setItem(slot, data.getDisplayItemStack());
                System.out.println(data);
                gui.showInventory(player);
                //player.openInventory(inventory);
            } else if (script.startsWith("消耗前置条件")) {
                int times = Integer.parseInt(script.split(" ")[1]);
                if ( !pool.runRequirement(player, times, pool.getDefaultRequirement())) {
                    GlobalConfig.TEMP.put(player.getUniqueId(), "0");
                    player.sendMessage(Message.ERROR_REQUIREMENT.getMessage());
                }
            }
        }
    }
}
