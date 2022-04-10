package com.samyyc.lottery.commands.command;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.commands.handler.CommandCondition;
import com.samyyc.lottery.enums.Message;
import com.samyyc.lottery.enums.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RewardGroupCommand {

    static class FileAndConfig {
        private File file;
        private YamlConfiguration config;

        public FileAndConfig(File file, YamlConfiguration config) {
            this.file = file;
            this.config = config;
        }

        public File getFile() {
            return file;
        }

        public YamlConfiguration getConfig() {
            return config;
        }
    }


    public static void save(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileAndConfig getConfig(String fileName) {
        File file = new File(Lottery.getInstance().getDataFolder(), "奖品组\\"+fileName+".yml");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileAndConfig(file, YamlConfiguration.loadConfiguration(file));
    }

    @CommandCondition(requiredArgLength = 3, identifier = "group", secondIdentifier = "create", requiredPerms = Permission.PERM_GROUP_CREATE)
    public static void createGroup(CommandSender sender, String[] args) {
        FileAndConfig fac = getConfig(args[2]);
        YamlConfiguration config = fac.getConfig();
        File file = fac.getFile();
        config.set("奖品列表", new ArrayList<>());
        save(config, file);
        sender.sendMessage(Message.SUCCESS_GROUP_CREATE.getMessage());
    }

    @CommandCondition(requiredArgLength = 4, identifier = "group", secondIdentifier = "add", requiredPerms = Permission.PERM_GROUP_ADD)
    public static void addRewardToGroup(CommandSender sender, String[] args) {
        FileAndConfig fac = getConfig(args[2]);
        YamlConfiguration config = fac.getConfig();
        File file = fac.getFile();

        List<String> rewardList = config.getStringList("奖品列表");
        rewardList.add(args[3]);
        config.set("奖品列表", rewardList);
        save(config, file);
        sender.sendMessage(Message.SUCCESS_GROUP_ADD.getMessage());
    }

    @CommandCondition(requiredArgLength = 4, identifier = "group", secondIdentifier = "remove", requiredPerms = Permission.PERM_GROUP_REMOVE)
    public static void removeRewardFromGroup(CommandSender sender, String[] args) {
        FileAndConfig fac = getConfig(args[2]);
        YamlConfiguration config = fac.getConfig();
        File file = fac.getFile();

        List<String> rewardList = config.getStringList("奖品列表");
        rewardList.remove(args[4]);
        config.set("奖品列表", rewardList);
        save(config, file);
        sender.sendMessage(Message.SUCCESS_GROUP_REMOVE.getMessage());
    }

}
