package com.samyyc.lottery.commands;

import com.samyyc.lottery.containers.InventoryContainer;
import com.samyyc.lottery.enums.Permission;
import com.samyyc.lottery.objects.LotteryInventory;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command
public class InventoryCommand {

    @CommandCondition(requiredArgLength = 1, identifier = "inventory", senderType = Player.class, requiredPerms = Permission.PERM_INVENTORY_USE)
    public static void openInventory(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        LotteryInventory lotteryInventory = InventoryContainer.getInventory(player.getUniqueId());
        player.openInventory(lotteryInventory.getInventory(0));
    }

}
