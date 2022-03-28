package com.samyyc.lottery;

import com.samyyc.lottery.apis.APIManager;
import com.samyyc.lottery.bstats.Metrics;
import com.samyyc.lottery.commands.CommandListener;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.containers.GuiContainer;
import com.samyyc.lottery.containers.InventoryContainer;
import com.samyyc.lottery.containers.PoolContainer;
import com.samyyc.lottery.containers.RewardContainer;
import com.samyyc.lottery.listeners.lotteryGUIListener;
import com.samyyc.lottery.miscs.AdvLotteryExpansion;
import com.samyyc.lottery.utils.APIUtils;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

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

        // 注册命令监听器
        this.getCommand("advlottery").setExecutor(new CommandListener());

        // 注册监听器
        Bukkit.getServer().getPluginManager().registerEvents(new lotteryGUIListener(), this);

        // 接入bStats
        int pluginId = 14784;
        Metrics metrics = new Metrics(this, pluginId);

        //Bukkit.getScheduler().runTaskTimer(this, () -> System.out.println(GlobalConfig.resultList), 10L, 0L);
        // TODO: 把GlobalConfig.floorList里的东西实装


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
