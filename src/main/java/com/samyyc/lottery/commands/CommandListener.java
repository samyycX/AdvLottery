package com.samyyc.lottery.commands;

import com.samyyc.lottery.objects.LotteryInventory;
import com.samyyc.lottery.objects.LotteryPool;
import com.samyyc.lottery.objects.LotteryReward;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.containers.PoolContainer;
import com.samyyc.lottery.containers.RewardContainer;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.TextUtil;
import com.samyyc.lottery.utils.WarningUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        LotteryPool lotteryPool;

        switch ( args[0].toLowerCase() ) {
            case "help":
                ExtraUtils.printHelpToPlayer(player);
            case "open":
                if (LotteryPool.checkExist(args[1])) {
                    lotteryPool = PoolContainer.getPool(args[1]);
                    lotteryPool.showLotteryPool(player);
                } else {
                    sender.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c未知的奖池: "+args[1]));
                }
                break;
            case "pool":
                if ( sender.isOp() ) {
                    if (args.length >= 3) {
                        // args[0] = "pool"
                        // args[1] = "奖池名称"
                        // args[2] = "参数create/addreward/setData"
                        // args[3] = "奖品ID"
                        switch (args[2].toLowerCase()) {
                            case "create":
                                PoolContainer.addPool(args[1], true);
                                sender.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&a奖池创建成功!"));
                                break;
                            case "addreward":
                                lotteryPool = PoolContainer.getPool(args[1]);
                                String rewardName = args[3];
                                lotteryPool.initializeReward(rewardName);
                                sender.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&a奖品添加成功!&c(如果奖品已在配置中存在，将不会做出任何改变)"));
                                break;
                            default:
                                sender.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c未知的参数: "+args[2]+", 请输入/advlottery help 获取帮助"));
                        }
                    } else {
                        sender.sendMessage(WarningUtil.COMMAND_ERROR.getMessage());
                    }
                } else {
                    sender.sendMessage(WarningUtil.PERMISSION_ERROR.getMessage());
                }
                break;
            case "reward":
                if ( sender.isOp() ) {
                    if(args.length>=3) {
                        // args[0] = reward
                        // args[1] = 奖品
                        // args[2] =create/setDisplayItem/setItem
                        String rewardName = args[1];
                        LotteryReward reward;
                        switch ( args[2].toLowerCase()) {
                            case "create":
                                reward = RewardContainer.getReward(rewardName);
                                break;
                            case "setdisplayitem":
                                reward = RewardContainer.getReward(rewardName);
                                reward.setDisplayItem(player.getInventory().getItemInMainHand());
                                break;
                            case "setitem":
                                reward = RewardContainer.getReward(rewardName);
                                reward.setItem(args[3], player.getInventory().getItemInMainHand());
                                break;
                        }
                    } else {
                        sender.sendMessage(WarningUtil.COMMAND_ERROR.getMessage());
                        ExtraUtils.printHelpToPlayer(player);
                    }
                } else {
                    sender.sendMessage(WarningUtil.PERMISSION_ERROR.getMessage());
                }
                break;
            case "inventory":
                Bukkit.getLogger().info(player.getName());
                LotteryInventory lotteryInventory = new LotteryInventory(player);
                player.openInventory(lotteryInventory.getInventory(0));
                break;
            case "reload":
                if (sender.isOp()) {
                    GlobalConfig.GUIScriptList.clear();
                } else {
                    sender.sendMessage(WarningUtil.PERMISSION_ERROR.getMessage());
                }
                break;
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
         * /advlottery pool 奖池 setData 奖品1 真实概率=10 显示概率=超稀有 物品限量=1 保底次数=100 保底限量=1
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
