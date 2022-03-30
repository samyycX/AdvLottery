package com.samyyc.lottery.commands;

import com.samyyc.lottery.containers.InventoryContainer;
import com.samyyc.lottery.enums.Permission;
import com.samyyc.lottery.objects.LotteryInventory;
import com.samyyc.lottery.objects.LotteryPool;
import com.samyyc.lottery.objects.LotteryReward;
import com.samyyc.lottery.containers.RewardContainer;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.enums.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        boolean success = false;
        Method successMethod = null;

        for (Class<?> commandClass : CommandContainer.getCommandClassesSet()) {
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
         * 需求:
         * TODO: 10连抽送1抽
         * TODO: 抽奖数据记录
         * TODO: 大奖限量
         * TODO: 可刷新奖池
         * TODO: placeholder API支持
         * TODO: 消耗物品进行抽奖
         *
         */

        /**
         *
         * 指令示例
         * /advlottery open 奖池
         * /advlottery pool 奖池 create
         * /advlottery pool 奖池 addreward 奖品1
         * /advlottery reward 奖品 create
         * /advlottery reward 奖品 setDisplayItem
         * /advlottery reward 奖品 setItem 物品
         *
         */

        //lotteryPool = new LotteryPool("test");
        //lotteryPool.showLotteryPool(player);

        return true;
    }
}
