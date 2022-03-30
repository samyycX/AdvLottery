package com.samyyc.lottery.utils;

import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.enums.Message;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.samyyc.lottery.Lottery;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.samyyc.lottery.apis.CustomLotteryTask;

public class APIUtils {

    public static boolean isVaultEnabled = false;
    public static boolean isPlayerPointsEnabled = false;
    public static boolean isPlaceholderAPIEnabled = false;

    private static PlayerPoints playerPoints;
    private static Economy economy;

    public static void hookDependPlugin() {
        // vault 经济插件
        if (Lottery.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Lottery.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                isVaultEnabled = (economy != null);
            }
        }
        // playerPoint 点券插件
        Plugin playerPointsPlugin = Lottery.getInstance().getServer().getPluginManager().getPlugin("PlayerPoints");
        playerPoints = (PlayerPoints) playerPointsPlugin;
        isPlayerPointsEnabled = (playerPoints != null);

        // placeholderAPI 插件
        isPlaceholderAPIEnabled = (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);

        if (isVaultEnabled) {
            Bukkit.getLogger().info(Message.SUCCESS_API_HOOK_VAULT.getMessage());
        } else {
            Bukkit.getLogger().info(Message.WARNING_API_HOOK_VAULT.getMessage());
        }
        if (isPlaceholderAPIEnabled) {
            Bukkit.getLogger().info(Message.SUCCESS_API_HOOK_PLACEHOLDERAPI.getMessage());
        } else {
            Bukkit.getLogger().info(Message.WARNING_API_HOOK_PLACEHOLDERAPI.getMessage());
        }
        if (isPlayerPointsEnabled) {
            Bukkit.getLogger().info(Message.SUCCESS_API_HOOK_PLAYERPOINTS.getMessage());
        } else {
            Bukkit.getLogger().info(Message.WARNING_API_HOOK_PLAYERPOINTS.getMessage());
        }
    }

    public static boolean takePlayerVaultEconomy(Player player, int amount) {
        if (!isVaultEnabled) return false;
        EconomyResponse economyResponse = economy.withdrawPlayer(player, amount);
        return economyResponse.transactionSuccess();
    }

    public static boolean addPlayerVaultEconomy(Player player, int amount) {
        if (!isVaultEnabled) return false;
        EconomyResponse economyResponse = economy.depositPlayer(player, amount);
        return economyResponse.transactionSuccess();
    }
    public static boolean takePlayerPoints(Player player, int amount) {
        return isPlayerPointsEnabled && playerPoints.getAPI().take(player.getUniqueId(), amount);
    }

    public static boolean addPlayerPoints(Player player, int amount) {
        return isPlayerPointsEnabled && playerPoints.getAPI().give(player.getUniqueId(), amount);
    }

    public static boolean hasVaultEconomy(Player player, int amount) {
        if (!isVaultEnabled) return false;
        double balance = economy.getBalance(player);
        return amount <= balance;
    }

    public static boolean hasPlayerPoints(Player player, int amount) {
        return isPlayerPointsEnabled && (amount <= playerPoints.getAPI().look(player.getUniqueId()));
    }

    public static boolean invokeCustomLotteryTask(String identifier, Player player, String task, int times, boolean run) {
        CustomLotteryTask customLotteryTask = GlobalConfig.taskMap.get(identifier);
        if ( customLotteryTask != null ) {
            boolean success = customLotteryTask.check(player, task, times);
            if (run) {
                if (success) {
                    customLotteryTask.run(player, task, times);
                }
            }
            return success;
        } else return false;
    }



}
