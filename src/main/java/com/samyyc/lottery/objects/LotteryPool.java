package com.samyyc.lottery.objects;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.utils.APIUtils;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.TextUtil;
import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

    private HashMap<LotteryData, Integer> lotteryChanceMap = new HashMap<>();

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

    public void invalidFilter(Player player) {
        if (isEnabled) {
            Iterator<LotteryData> iterator = lotteryChanceMap.keySet().iterator();
            while (iterator.hasNext()) {
                LotteryData data = iterator.next();
                data.initializePlayerData(player);
                if (data.getDisplayItemStack().getType() == Material.BARRIER && data.getDisplayItemStack().getItemMeta().getDisplayName().contains(TextUtil.convertColor("&c&l不可用"))) {
                    iterator.remove();
                }
            }

            Iterator<Map.Entry<LotteryData, Integer>> it = lotteryChanceMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<LotteryData, Integer> entry = it.next();
                entry.getKey().initializePlayerData(player);
                if (entry.getKey().getDisplayItemStack().getType() == Material.BARRIER || entry.getKey().getDisplayItemStack().getItemMeta().getDisplayName().contains(TextUtil.convertColor("&c&l不可用"))) {
                    it.remove();
                }
            }
        }
    }

    /**
     * 向玩家显示抽奖页面GUI
     * @param player 给谁打开GUI
     */
    public void showLotteryPool(Player player) {
        int i = 0;
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
        String[] split = args.split(" ");
        if (split.length == 2) {
            String poolName = split[0];
            String attribute = split[1];
            LotteryData data = (LotteryData) lotteryChanceMap.keySet().toArray()[0];
            if (attribute.equals("全服已抽此奖池次数")) {
                return String.valueOf(data.poolTotalTime);
            } else if (attribute.equals("玩家已抽此奖池次数")) {
                return String.valueOf(data.playerDataMap.get(player.getName()));
            } else {
                return null;
            }
        } else if (split.length == 3) {
            String rewardName = split[1];
            String attribute = split[2];
            for (LotteryData data : lotteryChanceMap.keySet() ) {
                if (data.getReward().getRewardName().equals(rewardName)) {
                    return String.valueOf(data.configMap.get(attribute));
                }
            }
        }
        return null;
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
                            player.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c您没有足够的物品!"));
                            return false;
                        }
                        break;
                    case "vault减钱":
                        if (!APIUtils.hasVaultEconomy(player, Integer.parseInt(arg) * times)) {
                            player.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c您没有足够的钱!"));
                            return false;
                        }
                        break;
                    case "playerpoints减点数":
                        if (!APIUtils.hasPlayerPoints(player, Integer.parseInt(arg) * times)) {
                            player.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c您没有足够的点券!"));
                            return false;
                        }
                        break;
                    case "拥有权限":
                        if (!player.hasPermission(arg)) {
                            player.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c您没有权限&e"+arg+"&c以抽奖!"));
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
                    case "个人广播":
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
            return true;
        } else return true;
    }

    public void initializeReward(String rewardName) {
        if ( config.getConfigurationSection("奖品列表."+rewardName) != null) {
            ConfigurationSection section = config.createSection("奖品列表."+rewardName);
            section.set("真实概率", 0);
            section.set("显示概率", "example");
            section.set("物品限量", 0);
            section.set("保底次数", 0);
            section.set("保底限量", 0);
            section.set("全服限制数量", 0);
            section.set("lores", new ArrayList<String>().add("测试"));
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LotteryData data = new LotteryData(config, name, rewardName, null);
        initialize(false, true, null);
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
                if (sender != null) sender.sendMessage(TextUtil.convertColor(GlobalConfig.PREFIX +"&c奖池不存在!"));
                return;
            }
        }
        if ( sender instanceof Player) {
            for ( LotteryData data : lotteryChanceMap.keySet() ) {
                data.initializePlayerData((Player) sender);
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        // 初始化奖池属性
        // 启用
        isEnabled = config.getBoolean("启用");

        // 奖励池物品获取 ("奖池A.奖品列表")
        ConfigurationSection rewardsSection = config.getConfigurationSection("奖品列表");
        Set<String> set = rewardsSection.getKeys(false);
        if (set.size() != 0) {
            for ( String rewardName : set ) {
                LotteryData lotteryData;
                if (sender instanceof Player) {
                     lotteryData = new LotteryData(config, name, rewardName, (Player) sender);
                } else {
                    lotteryData = new LotteryData(config, name, rewardName, null);
                }
                lotteryChanceMap.put(lotteryData, (int)lotteryData.configMap.get("真实概率"));


            }
        }

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


    public LotteryData roll() {
        Random random = new Random();

        int sum = 0;
        for ( int value : lotteryChanceMap.values() ) {
            sum+=value;
        }

        int rand = random.nextInt(sum)+1;

        for (Map.Entry<LotteryData, Integer> entry: lotteryChanceMap.entrySet() ) {
            rand -= entry.getValue();
            if (rand<=0) {
                return entry.getKey();
            }
        }
        return null;
    }

    public LotteryReward getRewardByItemstack(ItemStack itemStack) {
        for (LotteryData data : lotteryChanceMap.keySet() ) {
            ItemStack dataItemstack = data.getDisplayItemStack();
            if (
                    dataItemstack.getType().equals(itemStack.getType())
                    && dataItemstack.getAmount() == itemStack.getAmount()
                    && dataItemstack.getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName())
            ) {
                return data.getReward();
            }
        }
        return null;
    }

    public void refreshPlayerData(CommandSender player) {
        if (playerRefreshRequirement.isEmpty()) return;
        if (runRequirement(player, 1, playerRefreshRequirement))
        for (LotteryData data : lotteryChanceMap.keySet() ) {
            data.refreshPlayerData(player.getName());
        }
    }

    public void refreshGlobalData(CommandSender player) {
        if (globalRefreshRequirement.isEmpty()) return;
        if (runRequirement(player, 1, globalRefreshRequirement)) {
            for (LotteryData data : lotteryChanceMap.keySet()) {
                data.refreshGlobalData();
            }
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
