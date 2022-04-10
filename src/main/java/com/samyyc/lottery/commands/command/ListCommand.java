package com.samyyc.lottery.commands.command;

import com.samyyc.lottery.commands.handler.CommandCondition;
import com.samyyc.lottery.enums.Permission;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.FileUtil;
import org.bukkit.command.CommandSender;

public class ListCommand {

    @CommandCondition(requiredArgLength = 1, identifier = "poollist",requiredPerms = Permission.PERM_POOL_SHOWLIST)
    public static void showPoolList(CommandSender sender, String[] args) {
        int page = args.length == 2 ? Integer.parseInt(args[1]) : 0;
        ExtraUtils.iDontKnowHowToNameThisMethod("奖池列表", FileUtil.getAllFile(page, "奖池")).forEach(sender::sendMessage);
    }

    @CommandCondition(requiredArgLength = 1, identifier = "rewardlist",requiredPerms = Permission.PERM_REWARD_SHOWLIST)
    public static void showRewardList(CommandSender sender, String[] args) {
        int page = args.length == 2 ? Integer.parseInt(args[1]) : 0;
        ExtraUtils.iDontKnowHowToNameThisMethod("奖品列表", FileUtil.getAllFile(page, "奖品")).forEach(sender::sendMessage);
    }

    @CommandCondition(requiredArgLength = 1, identifier = "grouplist",requiredPerms = Permission.PERM_GROUP_SHOWLIST)
    public static void showRewardGroupList(CommandSender sender, String[] args) {
        int page = args.length == 2 ? Integer.parseInt(args[1]) : 0;
        ExtraUtils.iDontKnowHowToNameThisMethod("奖品组列表", FileUtil.getAllFile(page, "奖品组")).forEach(sender::sendMessage);
    }

}
