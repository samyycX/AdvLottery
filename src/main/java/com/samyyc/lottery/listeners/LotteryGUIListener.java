package com.samyyc.lottery.listeners;

import com.samyyc.lottery.containers.GuiContainer;
import com.samyyc.lottery.containers.InventoryContainer;
import com.samyyc.lottery.objects.LotteryGUI;
import com.samyyc.lottery.objects.LotteryInventory;
import com.samyyc.lottery.objects.LotteryResult;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.enums.Message;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LotteryGUIListener implements Listener {

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

        if (GuiContainer.get(e.getWhoClicked().getUniqueId()) != null) {
            if (!GlobalConfig.rollingPlayerList.contains((Player) e.getWhoClicked())) {
                LotteryGUI gui = GuiContainer.get(e.getWhoClicked().getUniqueId());
                if (e.getView().getItem(e.getSlot()).getType() != Material.AIR) {
                    gui.processClickedSlot(e.getSlot(), (Player) e.getWhoClicked());
                    e.setCancelled(true);
                }
            } else {
                e.getWhoClicked().sendMessage(Message.ERROR_ALREADY_IN_ROLLING.getMessage());
                e.setCancelled(true);
                return;
            }
            if (GlobalConfig.getResults(e.getWhoClicked().getUniqueId()) != null && !GlobalConfig.getResults(e.getWhoClicked().getUniqueId()).isEmpty()) {
                Iterator<LotteryResult> it = GlobalConfig.getResults(e.getWhoClicked().getUniqueId()).iterator();
                while (it.hasNext()) {
                    LotteryResult result = it.next();
                    if (result.getSlot() == e.getSlot()) {
                        result.execute();
                        it.remove();
                        e.getClickedInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                        ((Player) e.getWhoClicked()).updateInventory();
                        e.setCancelled(true);
                    }
                }
            }
        } else if (e.getView().getTitle().contains(TextUtil.convertColor("&b抽奖背包 - "))) {
            LotteryInventory lotteryInventory = InventoryContainer.getInventory(e.getWhoClicked().getUniqueId());
            Pattern pattern = Pattern.compile("(?<=第)(\\d+)(?=页)");
            Matcher matcher = pattern.matcher(e.getView().getTitle());
            matcher.find();
            lotteryInventory.processClickedSlot(e.getRawSlot(), Integer.parseInt(matcher.group(1)));
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent e) {
        if (GuiContainer.get(e.getPlayer().getUniqueId()) != null && !GlobalConfig.rollingPlayerList.contains((Player) e.getPlayer())) {
            if (!GlobalConfig.resultList.isEmpty()) {
                List<LotteryResult> resultList = GlobalConfig.resultList.get(e.getPlayer().getUniqueId());
                for (LotteryResult result : resultList ) {
                    LotteryInventory lotteryInventory = InventoryContainer.getInventory(e.getPlayer().getUniqueId());
                    lotteryInventory.addReward(result.getLotteryReward());
                }
                GlobalConfig.resultList.put(e.getPlayer().getUniqueId(), new ArrayList<>());
            }
            GuiContainer.remove(e.getPlayer().getUniqueId());
        }
    }
}
