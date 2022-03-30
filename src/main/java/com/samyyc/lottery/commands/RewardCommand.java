package com.samyyc.lottery.commands;

import com.samyyc.lottery.containers.RewardContainer;
import com.samyyc.lottery.objects.LotteryReward;
import com.samyyc.lottery.enums.Message;
import com.samyyc.lottery.enums.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command
public class RewardCommand {
    @CommandCondition(requiredArgLength = 3, identifier = "reward", secondIdentifier = "create", requiredPerms = Permission.PERM_REWARD_CREATE)
    public static void createReward(CommandSender sender, String[] args) {
        LotteryReward reward = RewardContainer.getReward(args[1]);
        sender.sendMessage(Message.SUCCESS_CREATE_REWARD.getMessage());
    }

    @CommandCondition(requiredArgLength = 3, identifier = "reward", secondIdentifier = "setdisplayitem", senderType = Player.class, requiredPerms = Permission.PERM_REWARD_SETDISPLAYITEM)
    public static void setDisplayItem(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        LotteryReward reward = RewardContainer.getReward(args[1]);
        reward.setDisplayItem(player.getInventory().getItemInMainHand());
        player.sendMessage(Message.SUCCESS_SET_ITEM.getMessage());
    }

    @CommandCondition(requiredArgLength = 4, identifier = "reward", secondIdentifier = "setitem", senderType = Player.class, requiredPerms = Permission.PERM_REWARD_SETITEM)
    public static void setItem(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        LotteryReward reward = RewardContainer.getReward(args[1]);
        reward.setItem(args[3], player.getInventory().getItemInMainHand());
        player.sendMessage(Message.SUCCESS_SET_ITEM.getMessage());
    }
}
