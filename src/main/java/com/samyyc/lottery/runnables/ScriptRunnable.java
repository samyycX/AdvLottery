package com.samyyc.lottery.runnables;

import com.samyyc.lottery.objects.LotteryData;
import com.samyyc.lottery.objects.LotteryPool;
import com.samyyc.lottery.objects.LotteryResult;
import com.samyyc.lottery.objects.LotteryReward;
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
                LotteryReward reward = pool.getRewardByItemstack(inventory.getItem(targetSlot));
                LotteryResult result = new LotteryResult(player, targetSlot, reward);
                if (GlobalConfig.resultList.get(player.getName()) == null) {
                    List<LotteryResult> list = new ArrayList<>();
                    list.add(result);
                    GlobalConfig.resultList.put(player.getName(), list);
                } else {
                    List<LotteryResult> list = GlobalConfig.resultList.get(player.getName());
                    list.add(result);
                    GlobalConfig.resultList.put(player.getName(), list);
                }
                LogUtils.addLog(player.getName()+" 在 "+pool.getName()+" 抽到了 "+result.getLotteryReward().getRewardName());
            } else if (script.startsWith("轮换物品")) {
                String[] split = script.split(" ");
                int originSlot;
                int targetSlot;
                originSlot = Integer.parseInt(split[1]);
                targetSlot = Integer.parseInt(split[2]);
                //inventory.setItem(targetSlot, inventory.getItem(originSlot));
                player.getOpenInventory().getTopInventory().setItem(targetSlot, inventory.getItem(originSlot));
            } else if (script.startsWith("替换物品")) {
                String[] split = script.split(" ");
                int originSlot;
                originSlot = Integer.parseInt(split[1]);
                ItemStack item = itemMap.get(split[2]);
                if (item != null) {
                    //inventory.setItem(originSlot, item);
                    player.getOpenInventory().getTopInventory().setItem(originSlot, item);
                }
            } else if (script.startsWith("播放音符")) {
                String[] split = script.split(" ");
                Instrument instrument = Instrument.valueOf(split[1]);
                Note.Tone tone = Note.Tone.valueOf(split[2]);
                player.playNote(player.getLocation(), instrument, Note.natural(1, tone));
            } else if (script.startsWith("随机奖品")) {
                LotteryData data = pool.roll();
                int slot = Integer.parseInt(script.split(" ")[1]);
                inventory.setItem(slot, data.getDisplayItemStack());
                player.openInventory(inventory);
            } else if (script.startsWith("消耗前置条件")) {
                int times = Integer.parseInt(script.split(" ")[1]);
                pool.runRequirement(player, times, pool.getDefaultRequirement());
            }
        }
    }
}
