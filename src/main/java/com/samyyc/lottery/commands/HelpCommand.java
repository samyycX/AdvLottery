package com.samyyc.lottery.commands;

import com.samyyc.lottery.enums.Permission;
import com.samyyc.lottery.utils.ExtraUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command
public class HelpCommand {

    @CommandCondition(requiredArgLength = 1, identifier = "help", requiredPerms = Permission.PERM_HELP)
    public static void sendHelp(CommandSender sender, String[] args) {
        ExtraUtils.printHelpToPlayer(sender);
    }

}
