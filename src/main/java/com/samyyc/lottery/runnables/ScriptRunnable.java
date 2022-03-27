package com.samyyc.lottery.runnables;

import com.samyyc.lottery.containers.GuiContainer;
import com.samyyc.lottery.objects.*;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.utils.LogUtils;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptRunnable implements Runnable {

    private String script;
    private Inventory inventory;
    private Player player;
    private LotteryPool pool;
    private Map<String, ItemStack> itemMap;

    public ScriptRunnable(String script, Player player, LotteryPool pool, Map<String, ItemStack> itemMap) {
        this.script = script;
        this.inventory = player.getOpenInventory().getTopInventory();
        this.player = player;
        this.pool = pool;
        this.itemMap = itemMap;
    }

    public ScriptRunnable(String script, Inventory inventory, Player player, LotteryPool pool, Map<String, ItemStack> itemMap) {
        this.script = script;
        this.inventory = inventory;
        this.player = player;
        this.pool = pool;
        this.itemMap = itemMap;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (script.startsWith("出奖")) {
                String[] split = script.split(" ");
                int targetSlot = Integer.parseInt(split[1]);
                LotteryGUI gui = GuiContainer.getGUI(player.getUniqueId());
                LotteryReward reward = pool.getRewardByItemstack(gui.inventory().getItem(targetSlot));
                LotteryResult result = new LotteryResult(player, targetSlot, reward);
                GlobalConfig.putResult(player.getName(), result);
                System.out.println("调用");
                    //List<LotteryResult> list = GlobalConfig.resultList.get(player.getName());
                    //list.add(result);
                LogUtils.addLog(player.getName()+" 在 "+pool.getName()+" 抽到了 "+result.getLotteryReward().getRewardName());
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
                LotteryGUI gui = GuiContainer.getGUI(player.getUniqueId());
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
                    LotteryGUI gui = GuiContainer.getGUI(player.getUniqueId());
                    gui.inventory().setItem(originSlot, item);
                    gui.showInventory(player);
                    //player.getOpenInventory().getTopInventory().setItem(originSlot, item);
                }
            } else if (script.startsWith("播放音符")) {
                String[] split = script.split(" ");
                Instrument instrument = Instrument.valueOf(split[1]);
                Note.Tone tone = Note.Tone.valueOf(split[2]);
                player.playNote(player.getLocation(), instrument, Note.natural(1, tone));
            } else if (script.startsWith("随机奖品")) {
                LotteryData data = pool.roll();
                int slot = Integer.parseInt(script.split(" ")[1]);
                LotteryGUI gui = GuiContainer.getGUI(player.getUniqueId());
                gui.inventory().setItem(slot, data.getDisplayItemStack());
                gui.showInventory(player);
                //player.openInventory(inventory);
            } else if (script.startsWith("消耗前置条件")) {
                int times = Integer.parseInt(script.split(" ")[1]);
                pool.runRequirement(player, times, pool.getDefaultRequirement());
            }
        }
    }
}
