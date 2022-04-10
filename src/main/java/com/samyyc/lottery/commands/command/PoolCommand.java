package com.samyyc.lottery.commands.command;

import com.samyyc.lottery.commands.handler.Command;
import com.samyyc.lottery.commands.handler.CommandCondition;
import com.samyyc.lottery.containers.PoolContainer;
import com.samyyc.lottery.objects.LotteryPool;
import com.samyyc.lottery.enums.Message;
import com.samyyc.lottery.enums.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command
public class PoolCommand {

    @CommandCondition(requiredArgLength = 1, identifier = "open", senderType = Player.class, requiredPerms = Permission.PERM_POOL_OPEN)
    public static void open(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (LotteryPool.checkExist(args[1])) {
            LotteryPool lotteryPool = PoolContainer.getPool(args[1]);
            lotteryPool.showLotteryPool(player);
        } else {
            player.sendMessage(Message.ERROR_UNKNOWN_POOL.getMessage().replace("{poolname}",""));
        }
    }

    @CommandCondition(requiredArgLength = 3, identifier = "pool", secondIdentifier = "create", requiredPerms = Permission.PERM_POOL_CREATE)
    public static void createPool(CommandSender sender, String[] args) {
        PoolContainer.addPool(args[1], true);
        sender.sendMessage(Message.SUCCESS_CREATE_POOL.getMessage());;
    }

    @CommandCondition(requiredArgLength = 4, identifier = "pool", secondIdentifier = "addreward", requiredPerms = Permission.PERM_POOL_ADDREWARD)
    public static void addRewardToPool(CommandSender sender, String[] args) {
        LotteryPool lotteryPool = PoolContainer.getPool(args[1]);
        String rewardName = args[3];
        lotteryPool.initializeReward(rewardName);
        sender.sendMessage(Message.SUCCESS_ADD_ITEM.getMessage());
    }
}
