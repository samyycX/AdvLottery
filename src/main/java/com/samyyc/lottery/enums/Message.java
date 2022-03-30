package com.samyyc.lottery.enums;

import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.command.CommandSender;

public enum Message {

    ERROR_PERMISSION("&c您没有足够的权限使用该指令!"),
    ERROR_FILE("&c操作文件时出现错误!"),
    ERROR_MATERIAL("&c配置文件中的物品材质出现错误！"),
    ERROR_COMMAND("&c命令使用方法出错, 请输入/advlottery help 获取帮助"),
    ERROR_REQUIREMENT("&c您没有足够的条件抽奖/刷新奖池!"),
    ERROR_UNKNOWN_POOL("&c未知的奖池: {poolname}"),
    ERROR_UNKNOWN_ARGUMENT("&c未知的参数: {argument}, 请输入/advlottery help 获取帮助"),
    ERROR_ALREADY_IN_ROLLING("&c您已经在抽奖了!"),

    ERROR_UNKNOWN_INSTRUMENT("&c未知的乐器: {insname}"),
    ERROR_UNKNOWN_TONE("&c未知的音符: {tonename}"),


    WARNING_NOT_ENOUGH_ITEM("&c您没有足够的物品!"),
    WARNING_NOT_ENOUGH_VAULT_MONEY("&c您没有足够的钱!"),
    WARNING_NOT_ENOUGH_PLAYERPOINTS_CREDIT("&c您没有足够的点券!"),
    WARNING_NO_PERMISSION("&c您没有权限&e{permission}&c以抽奖!"),

    WARNING_API_HOOK_VAULT("&c未找到Vault插件!(&e不影响插件使用"),
    WARNING_API_HOOK_PLAYERPOINTS("&c未找到PlaceHolderAPI插件!(&e不影响插件使用"),
    WARNING_API_HOOK_PLACEHOLDERAPI("&c未找到PlayerPoints插件!(&e不影响插件使用)"),


    SUCCESS_RELOAD("&a插件重载成功!"),
    SUCCESS_CREATE_POOL("&a奖池创建成功!"),
    SUCCESS_CREATE_REWARD("&a奖品创建成功！"),
    SUCCESS_ADD_ITEM("&a奖品添加成功!&c(如果奖品已在配置中存在，将不会做出任何改变)"),
    SUCCESS_SET_ITEM("&a成功设置物品！"),

    SUCCESS_API_HOOK_VAULT("&a已找到并成功链接Vault插件!"),
    SUCCESS_API_HOOK_PLAYERPOINTS("&a已找到并成功链接PlayerPoints插件!"),
    SUCCESS_API_HOOK_PLACEHOLDERAPI("&a已找到并成功链接PlaceHolderAPI插件!"),


    LOG_ROLL_SUCCESS("&a{playername} 在 {poolname} 抽到了 {rewardname}");


    private final String message;

    Message(String message) {
        this.message = TextUtil.convertColor(GlobalConfig.PREFIX + message);
    }

    public String getMessage() {
        return message;
    }

}
