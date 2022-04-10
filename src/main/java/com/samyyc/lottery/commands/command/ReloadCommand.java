package com.samyyc.lottery.commands.command;

import com.samyyc.lottery.commands.handler.Command;
import com.samyyc.lottery.commands.handler.CommandCondition;
import com.samyyc.lottery.enums.Message;
import com.samyyc.lottery.enums.Permission;
import com.samyyc.lottery.utils.ExtraUtils;
import org.bukkit.command.CommandSender;

@Command
public class ReloadCommand {

    @CommandCondition(requiredArgLength = 1, identifier = "reload", requiredPerms = Permission.PERM_RELOAD)
    public static void reload(CommandSender sender, String[] args) {
        ExtraUtils.destroy();
        sender.sendMessage(Message.SUCCESS_RELOAD.getMessage());
    }

}
