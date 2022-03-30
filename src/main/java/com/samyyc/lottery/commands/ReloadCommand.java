package com.samyyc.lottery.commands;

import com.samyyc.lottery.enums.Message;
import com.samyyc.lottery.enums.Permission;
import com.samyyc.lottery.utils.ExtraUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command
public class ReloadCommand {

    @CommandCondition(requiredArgLength = 1, identifier = "reload", requiredPerms = Permission.PERM_RELOAD)
    public static void reload(CommandSender sender, String[] args) {
        ExtraUtils.destroy();
        sender.sendMessage(Message.SUCCESS_RELOAD.getMessage());
    }

}
