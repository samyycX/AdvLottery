package com.samyyc.lottery;

import com.samyyc.lottery.apis.APIManager;
import com.samyyc.lottery.bstats.Metrics;
import com.samyyc.lottery.commands.handler.CommandCondition;
import com.samyyc.lottery.commands.handler.CommandContainer;
import com.samyyc.lottery.commands.handler.CommandListener;
import com.samyyc.lottery.listeners.LotteryGUIListener;
import com.samyyc.lottery.miscs.AdvLotteryExpansion;
import com.samyyc.lottery.utils.APIUtils;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class Lottery extends JavaPlugin {

    private static Lottery instance;

    @Override
    public void onEnable() {
        // 初始化instance
        instance = this;

        // 链接API
        APIUtils.hookDependPlugin();
        if (APIUtils.isPlaceholderAPIEnabled) new AdvLotteryExpansion().register();

        // 初始化配置文件
        ExtraUtils.initConfigurations();

        // 注册Logger
        LogUtils.initLogger(false);

        // 初始化子命令
        CommandContainer.init();

        // 注册命令监听器
        this.getCommand("advlottery").setExecutor(new CommandListener());

        // 注册监听器
        Bukkit.getServer().getPluginManager().registerEvents(new LotteryGUIListener(), this);

        // 接入bStats
        int pluginId = 14784;
        Metrics metrics = new Metrics(this, pluginId);

        //Bukkit.getScheduler().runTaskTimer(this, () -> System.out.println(GlobalConfig.resultList), 10L, 0L);
        // TODO: 把GlobalConfig.floorList里的东西实装

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> nameList = new ArrayList<>();
            for (Class<?> commandClass : CommandContainer.getCommandHandlerSet()) {
                for (Method method : commandClass.getDeclaredMethods()) {
                    CommandCondition annotation = method.getAnnotation(CommandCondition.class);
                    nameList.add(annotation.identifier());
                }
            }
            return nameList;
        } else if (args.length == 3){
            List<String> nameList = new ArrayList<>();
            for (Class<?> commandClass : CommandContainer.getCommandHandlerSet()) {
                for (Method method : commandClass.getDeclaredMethods()) {
                    CommandCondition annotation = method.getAnnotation(CommandCondition.class);
                    nameList.add(annotation.secondIdentifier());
                }
            }
            return nameList;
        } else {
            return null;
        }
    }

    @Override
    public void onDisable() {
        ExtraUtils.destroy();
    }

    public static APIManager getAPI() {
        return new APIManager();
    }

    public static Lottery getInstance() {
        return instance;
    }
}
