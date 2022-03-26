package com.samyyc.lottery.utils;

import com.samyyc.lottery.configs.GlobalConfig;
import org.bukkit.command.CommandSender;

public enum WarningUtil {

    PERMISSION_ERROR(TextUtil.convertColor(GlobalConfig.PREFIX+"&c您没有足够的权限使用该指令!")),
    FILE_ERROR(TextUtil.convertColor(GlobalConfig.PREFIX+"&c操作文件时出现错误!")),
    MATERIAL_ERROR(TextUtil.convertColor(GlobalConfig.PREFIX+"&c配置文件中的物品材质出现错误！")),
    COMMAND_ERROR(TextUtil.convertColor(GlobalConfig.PREFIX +"&c命令使用方法出错, 请输入/advlottery help 获取帮助"));

    private final String message;

    private WarningUtil(String message) {
        this.message = TextUtil.convertColor(message);
    }

    public String getMessage() {
        return message;
    }

    public static void poolConfigError(String name) {
        // TODO: 添加代码
    }
    public static void rewardConfigError(String name) {
        // TODO: 添加代码
    }
    public static void materialError(String name) {
        // TODO: 添加代码
    }
    public static void permissionWarning(CommandSender sender) {
        sender.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c您没有足够的权限使用该指令!"));
    }


}
