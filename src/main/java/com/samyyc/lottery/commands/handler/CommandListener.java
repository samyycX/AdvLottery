package com.samyyc.lottery.commands.handler;

import com.samyyc.lottery.enums.Permission;
import com.samyyc.lottery.enums.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        boolean success = false;
        Method successMethod = null;

        for (Class<?> commandClass : CommandContainer.getCommandHandlerSet()) {
            for (Method method : commandClass.getDeclaredMethods()) {
                CommandCondition annotation = method.getAnnotation(CommandCondition.class);
                if (annotation == null) {
                    continue;
                }

                if (args.length < annotation.requiredArgLength()) {
                    continue;
                }

                if (!args[0].equalsIgnoreCase(annotation.identifier())) {
                    continue;
                }

                if (!annotation.secondIdentifier().equals("") && !args[2].equalsIgnoreCase(annotation.secondIdentifier())) {
                    continue;
                }

                if (!annotation.senderType().isAssignableFrom(sender.getClass())) {
                    continue;
                }

                if (annotation.requiredPerms() == Permission.PERM_DEFAULT || sender.hasPermission(annotation.requiredPerms().perm())) {
                    success = true;
                }
                successMethod = method;

            }

        }
        if (successMethod != null) {
            if (!success) {
                sender.sendMessage(Message.ERROR_PERMISSION.getMessage());
            } else {
                try {
                    successMethod.invoke(null, sender, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } else {
            sender.sendMessage(Message.ERROR_COMMAND.getMessage());
        }
        /**
         * ??????:
         * TODO: 10?????????1???
         * TODO: ??????????????????
         * TODO: ????????????
         * TODO: ???????????????
         * TODO: placeholder API??????
         * TODO: ????????????????????????
         *
         */

        /**
         *
         * ????????????
         * /advlottery open ??????
         * /advlottery pool ?????? create
         * /advlottery pool ?????? addreward ??????1
         * /advlottery reward ?????? create
         * /advlottery reward ?????? setDisplayItem
         * /advlottery reward ?????? setItem ??????
         *
         */

        //lotteryPool = new LotteryPool("test");
        //lotteryPool.showLotteryPool(player);

        return true;
    }
}
