package com.samyyc.lottery.commands.command;

import com.samyyc.lottery.commands.handler.Command;
import com.samyyc.lottery.commands.handler.CommandCondition;
import com.samyyc.lottery.enums.Permission;
import com.samyyc.lottery.utils.ExtraUtils;
import org.bukkit.command.CommandSender;

@Command
public class HelpCommand {

    @CommandCondition(requiredArgLength = 1, identifier = "help", requiredPerms = Permission.PERM_HELP)
    public static void sendHelp(CommandSender sender, String[] args) {
        ExtraUtils.printHelpToPlayer(sender);
    }

}
