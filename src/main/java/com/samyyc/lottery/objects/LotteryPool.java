package com.samyyc.lottery.objects;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.utils.APIUtils;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.enums.Message;
import com.samyyc.lottery.utils.TextUtil;
import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * LotteryPool奖池对象
 *
 */
public class LotteryPool {

    // 配置文件
    private File file;
    private YamlConfiguration config;

    // 奖池名称
    private String name = null;
    // 奖池是否启用
    private boolean isEnabled = false;
    // 奖池所有奖品
    //private LinkedList<LotteryData> lotteryDataList = new LinkedList<>();
    // 奖池所有奖品(带概率)
    //private List<LotteryData> lotteryDataMap = new ArrayList<>();

    private LotteryPoolData poolData;

    // 前置条件
    private List<String> requirement = new ArrayList<>();
    private List<String> playerRefreshRequirement = new ArrayList<>();
    private List<String> globalRefreshRequirement = new ArrayList<>();
    // GUI名称
    private String GUIName;

    private CommandSender sender;

    /**
     *
     * 构造器
     * @param poolname 奖池名称
     * @param isOp 玩家是否为op, 如果为true, 则创建配置
     */
    public LotteryPool(String poolname, boolean isOp) {
        this.name = poolname;
        initialize(false, isOp, null);
    }

    public Map<String, LotteryData> invalidFilter(Player player) {
        return poolData.invalidFilter(player);
    }

    /**
     * 向玩家显示抽奖页面GUI
     * @param player 给谁打开GUI
     */
    public void showLotteryPool(Player player) {
        if (isEnabled) {
            invalidFilter(player);

            LotteryGUI lotteryGUI = new LotteryGUI(this, player);
            lotteryGUI.showInventory(player);

        } else {
            player.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c该奖池尚未被启用!"));
        }

    }
    // %奖池A_aa%
    // %奖池A_奖品A_全服已出此奖品次数%
    public String getRewardData(Player player, String args) {

        String[] split = args.split("_");
        if (split.length == 2) {
            String attribute = split[1];
            return poolData.getAttribute(attribute, null, player);
        } else if (split.length == 3) {
            String rewardName = split[1];
            String attribute = split[2];
            return poolData.getAttribute(attribute, rewardName, player);
        }
        return null;
    }

    public LotteryData getReward(String rewardName) {
        return poolData.getReward(rewardName);
    }

    /**
     * 运行处理前置条件
     * @param sender 受处理玩家
     * @param times 处理几次(N连抽)
     * @return 处理是否成功
     */
    public boolean runRequirement(CommandSender sender, int times, List<String> requirement) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            return true;
        }

        if ( !requirement.isEmpty() ) {
            int line = 1;

            // 首先预处理一波，预防出现第一个条件符合并执行，而第二个条件不符合，导致吞了玩家的物品
            for ( String task : requirement) {
                String[] split = task.split(" ", 2);
                String type = split[0];
                String arg = split[1];
                switch (type.toLowerCase()) {
                    case "消耗物品":
                        String itemName = arg.split(" ")[0];
                        int itemAmount = Integer.parseInt(arg.split(" ")[1]);
                        ConfigurationSection itemstackSection = config.getConfigurationSection("物品预处理列表."+itemName);
                        ItemStack itemStack = ExtraUtils.generateItemstackFromYml(itemstackSection);
                        boolean isConsumeItemSuccess = ExtraUtils.checkItemstackInInventory(player, itemStack, itemAmount * times, false);
                        if (!isConsumeItemSuccess) {
                            player.sendMessage(Message.WARNING_NOT_ENOUGH_ITEM.getMessage());
                            return false;
                        }
                        break;
                    case "vault减钱":
                        if (!APIUtils.hasVaultEconomy(player, Integer.parseInt(arg) * times)) {
                            player.sendMessage(Message.WARNING_NOT_ENOUGH_VAULT_MONEY.getMessage());
                            return false;
                        }
                        break;
                    case "playerpoints减点数":
                        if (!APIUtils.hasPlayerPoints(player, Integer.parseInt(arg) * times)) {
                            player.sendMessage(Message.WARNING_NOT_ENOUGH_PLAYERPOINTS_CREDIT.getMessage());
                            return false;
                        }
                        break;
                    case "拥有权限":
                        if (!player.hasPermission(arg)) {
                            player.sendMessage(Message.WARNING_NO_PERMISSION.getMessage().replace("{permission}",arg));
                            return false;
                        }
                    default:
                        if(!APIUtils.invokeCustomLotteryTask(type, player, task, Integer.parseInt(arg) * times, false)) {
                            return false;
                        }
                }

            }

            for ( String task : requirement) {
                String[] split = task.split(" ", 2);
                String type = split[0];
                String arg = split[1];
                arg = arg.replace("{player}", player.getName());
                switch (type.toLowerCase()) {
                    case "消耗物品":
                        String itemName = arg.split(" ")[0];
                        int itemAmount = Integer.parseInt(arg.split(" ")[1]);
                        ConfigurationSection itemstackSection = config.getConfigurationSection("物品预处理列表." + itemName);
                        ItemStack itemStack = ExtraUtils.generateItemstackFromYml(itemstackSection);
                        ExtraUtils.checkItemstackInInventory(player, itemStack, itemAmount * times, true);
                        break;
                    case "vault减钱":
                        APIUtils.takePlayerVaultEconomy(player, Integer.parseInt(arg) * times);
                        break;
                    case "playerpoints减点数":
                        APIUtils.takePlayerPoints(player, Integer.parseInt(arg) * times);
                        break;
                    //  - 后台运行指令 abc
                    //  - 玩家运行指令 abc
                    //  - 全服广播 123
                    //  - 个人广播 123
                    case "后台运行指令":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), arg);
                    case "玩家运行指令":
                        Bukkit.dispatchCommand(player, arg);
                    case "全服广播":
                        Bukkit.broadcastMessage(arg);
                    case "玩家广播":
                        player.sendMessage(arg);
                    default:
                        if (APIUtils.invokeCustomLotteryTask(type, player, task, Integer.parseInt(arg) * times, false)) {
                            APIUtils.invokeCustomLotteryTask(type, player, task, Integer.parseInt(arg) * times, true);
                        } else {
                            Bukkit.getLogger().info(TextUtil.convertColor(GlobalConfig.PREFIX +"&c奖池"+name+" - 第"+line+"行: 未知的属性(或出错): "+type));
                        }
                }
                line++;
            }
        }
        return true;
    }

    public void initializeReward(String rewardName) {
       poolData.initReward(rewardName);
    }

    /**
     * 初始化
     * @param autoRefreshActivePool 是否刷新activepool配置
     * @param isOp 是否启用文件新建
     * @param sender 发送者
     */
    public void initialize(boolean autoRefreshActivePool, boolean isOp, @Nullable CommandSender sender) {
        // 初始化配置文件
        file = new File(Lottery.getInstance().getDataFolder(), "奖池/"+name+".yml");
        if (!file.exists()) {
            if (isOp) {
                try {
                    file.createNewFile();
                    File template = new File(Lottery.getInstance().getDataFolder(), "奖池/example.yml");
                    FileUtil.copy(template, file);
                    config = YamlConfiguration.loadConfiguration(file);
                    config.set("启用", true);
                    config.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (sender != null) sender.sendMessage(Message.ERROR_UNKNOWN_POOL.getMessage().replace("{poolname}",name));
                return;
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        // 初始化奖池属性
        // 启用
        isEnabled = config.getBoolean("启用");

        // 奖品数据
        poolData = new LotteryPoolData(name, config);

        //初始化前置条件
        requirement = config.getStringList("前置条件");
        if (config.contains("刷新玩家前置条件")) {
            playerRefreshRequirement = config.getStringList("刷新玩家前置条件");
        }
        if (config.contains("刷新全服前置条件")) {
            globalRefreshRequirement = config.getStringList("刷新全服前置条件");
        }

        // 获取GUI
        GUIName = (config.getString("gui") != null) ? config.getString("gui") : "example";
    }


    public LotteryData roll(Map<String, LotteryData> dataMap) {
        return poolData.roll(dataMap);
    }


    public LotteryData getDataByItemstack(ItemStack itemStack) {
        return poolData.getDataByItemstack(itemStack);
    }

    public void increaseRollTime(Player player) {
        poolData.increaseRollTime(player);
    }

    public void refreshPlayerData(Player player) {
        if (playerRefreshRequirement.isEmpty()) return;
        if (runRequirement(player, 1, playerRefreshRequirement)) {
            poolData.refreshPlayerData(player);
        }
    }

    public void refreshGlobalData(CommandSender player) {
        if (globalRefreshRequirement.isEmpty()) return;
        if (runRequirement(player, 1, globalRefreshRequirement)) {
            poolData.refreshGlobalData();
        }
    }

    /*
    public LotteryGUI getGUI() {
        return new LotteryGUI(this,);
    }

     */

    public static boolean checkExist(String poolname) {
        File file = new File(Lottery.getInstance().getDataFolder(), "奖池/"+poolname+".yml");
        return file.exists();
    }

    public String getName() {
        return name;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public String getGUIName() {
        return GUIName;
    }

    public List<String> getDefaultRequirement() {
        return requirement;
    }

}
