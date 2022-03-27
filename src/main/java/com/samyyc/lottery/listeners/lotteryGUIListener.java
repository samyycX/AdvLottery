package com.samyyc.lottery.listeners;

import com.samyyc.lottery.objects.LotteryGUI;
import com.samyyc.lottery.objects.LotteryInventory;
import com.samyyc.lottery.objects.LotteryResult;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class lotteryGUIListener implements Listener {

    @EventHandler
    public static void onPlayerInteractLotteryGUIEvent(InventoryClickEvent e) {

        if (e.getRawSlot() > 53) {e.setCancelled(true);
            return;
        }

        try {
            if (e.getView().getItem(e.getSlot()).getType().equals(Material.AIR)) return;
        } catch (NullPointerException ignored) {
            return;
        }

        if (e.getView().getTitle().contains(TextUtil.convertColor("&b抽奖 - "))) {
            if (!GlobalConfig.rollingPlayerList.contains((Player) e.getWhoClicked())) {
                LotteryGUI gui = new LotteryGUI(e.getView().getTitle().replaceAll(TextUtil.convertColor("&b抽奖 - &e"), ""), (Player) e.getWhoClicked());
                if (e.getView().getItem(e.getSlot()).getType() != Material.AIR) {
                    gui.processClickedSlot(e.getSlot(), (Player) e.getWhoClicked());
                    e.setCancelled(true);
                }
            } else {
                e.getWhoClicked().sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c您已经在抽奖了!"));
                e.setCancelled(true);
                return;
            }
            if (!GlobalConfig.resultList.isEmpty()) {
                Iterator<LotteryResult> it = GlobalConfig.resultList.get(e.getWhoClicked().getName()).iterator();
                while (it.hasNext()) {
                    LotteryResult result = it.next();
                    System.out.println(result.getSlot());
                    System.out.println(e.getSlot());
                    if (result.getSlot() == e.getSlot()) {
                        result.getLotteryReward().execute(result.getPlayer());
                        it.remove();
                        e.getClickedInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                        ((Player) e.getWhoClicked()).updateInventory();
                        e.setCancelled(true);
                    }
                }
            }
        } else if (e.getView().getTitle().contains(TextUtil.convertColor("&b抽奖背包 - "))) {
            LotteryInventory lotteryInventory = new LotteryInventory((Player) e.getWhoClicked());
            Pattern pattern = Pattern.compile("(?<=第)(\\d+)(?=页)");
            Matcher matcher = pattern.matcher(e.getView().getTitle());
            matcher.find();
            lotteryInventory.processClickedSlot(e.getRawSlot(), Integer.parseInt(matcher.group(1)));
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent e) {
        if (e.getView().getTitle().contains(TextUtil.convertColor("&b抽奖 - "))) {
            if (!GlobalConfig.resultList.isEmpty()) {
                Iterator<LotteryResult> it = GlobalConfig.resultList.get(e.getPlayer().getName()).iterator();
                while (it.hasNext()) {
                    LotteryInventory lotteryInventory = new LotteryInventory((Player)  e.getPlayer());
                    lotteryInventory.addReward(it.next().getLotteryReward());
                    it.remove();
                }
            }
        }
    }
}
