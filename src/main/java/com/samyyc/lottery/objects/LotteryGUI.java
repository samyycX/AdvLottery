package com.samyyc.lottery.objects;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.containers.GuiContainer;
import com.samyyc.lottery.containers.PoolContainer;
import com.samyyc.lottery.runnables.ScriptRunnable;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.FloorUtil;
import com.samyyc.lottery.utils.TextUtil;
import com.samyyc.lottery.enums.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LotteryGUI {

    private String GUIName;

    private String poolName;

    private YamlConfiguration config;

    private Inventory inventory;

    private LotteryPool pool;

    private Map<String, ItemStack> itemMap = new HashMap<>();

    private Player player;

    private Map<String, LotteryData> dataMap;

    public LotteryGUI(String title, Player player) {
        this.pool = PoolContainer.getPool(title);
        dataMap = pool.invalidFilter(player);
        this.poolName = pool.getName();
        this.GUIName = pool.getGUIName();
        this.player = player;
        initialize();
    }

    public LotteryGUI(LotteryPool pool, Player player) {
        this.pool = pool;
        dataMap = pool.invalidFilter(player);
        this.poolName = pool.getName();
        this.GUIName = pool.getGUIName();
        this.player = player;
        initialize();
    }

    public void showInventory(Player player) {
        player.openInventory(inventory);
        GuiContainer.add(player.getUniqueId(), this);
    }

    public void getInventoryFromYml() {
        String type;
        int length = config.getInt("设置.长度");
        int width = config.getInt("设置.宽度");
        InventoryType inventoryType;
        if ((type = config.getString("设置.类型")) != null) {
            inventoryType = InventoryType.valueOf(type);
            if (inventoryType!=InventoryType.CHEST) {
                inventory = Bukkit.createInventory(null, inventoryType, TextUtil.convertColor("&b抽奖 - &e"+poolName));
            } else {
                inventory = Bukkit.createInventory(null, length*width, TextUtil.convertColor("&b抽奖 - &e"+poolName));
            }
        } else {
            inventory = Bukkit.createInventory(null, length*width, TextUtil.convertColor("&b抽奖 - &e"+poolName));
        }

        ConfigurationSection GUISection = config.getConfigurationSection("GUI");
        for (String key : GUISection.getKeys(false)) {
            int slot = Integer.parseInt(key);
            String itemName = GUISection.getString(key);
            if (!itemName.matches("(?<=\\{奖品\\.).*(?=})")) {
                ConfigurationSection itemSection = config.getConfigurationSection("物品列表").getConfigurationSection(itemName);
                ItemStack itemStack = ExtraUtils.generateItemstackFromYml(itemSection);
                inventory.setItem(slot, itemStack);
            } else {
                inventory.setItem(slot, pool.getReward(itemName).getDisplayItemStack());
            }
        }

        for (String key : config.getConfigurationSection("物品列表").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("物品列表").getConfigurationSection(key);
            ItemStack itemStack = ExtraUtils.generateItemstackFromYml(section);
            if (itemStack != null) itemMap.put("{物品."+key+"}", itemStack);
        }

    }

    public void initialize() {
        File file = new File(Lottery.getInstance().getDataFolder(), "gui页面/" + GUIName + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                File template = new File(Lottery.getInstance().getDataFolder(), "gui页面/example.yml");
                FileUtil.copy(template, file);
            } catch (IOException e) {
                e.printStackTrace();
                Bukkit.getLogger().info(Message.ERROR_FILE.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        getInventoryFromYml();
        initGUIScript();
    }

    public Map<String, ItemStack> getItemstacks() {
        return itemMap;
    }

    public void initGUIScript() {
        ConfigurationSection section = config.getConfigurationSection("脚本列表");
        if (section != null) {
            List<String> processable = new ArrayList<>();
            if (GlobalConfig.GUIScriptList.isEmpty()) {
                // 持久化储存
                for (String scriptName : section.getKeys(false)) {

                    HashMap<String, String> variableMap = new HashMap<>();
                    //处理变量
                    //List<String> variables = section.getStringList(scriptName + ".变量");
                    ConfigurationSection variableSection = section.getConfigurationSection(scriptName).getConfigurationSection("变量列表");
                    if (variableSection != null) {
                    for (String key : variableSection.getKeys(false)) {
                        String type = variableSection.getString(key+".类型");
                        switch (type.toLowerCase()) {
                            case "string":
                            case "字符串":
                            case "文本":
                                String data = variableSection.getString(key + ".数据");
                                variableMap.put(key, data);
                                break;
                            case "stringlist":
                            case "字符串列表":
                            case "文本列表":
                                List<String> data2 = variableSection.getStringList(key + ".数据");
                                for (int i = 0; i < data2.size(); i++) {
                                    variableMap.put(key + "." + i, data2.get(i));
                                }
                                variableMap.put(key + ".大小", String.valueOf(data2.size()));
                                variableMap.put(key + ".首元素", data2.get(0));
                                variableMap.put(key + ".尾元素", data2.get(data2.size() - 1));
                                break;
                            }
                        }
                    }


                    String scriptBlockName = "";
                    List<String> scripts = section.getStringList(scriptName + ".脚本");
                    List<String> block = new ArrayList<>();
                    HashMap<String, LotteryScript> scriptList = new HashMap<>();

                    boolean startPushingBlock = false;
                    System.out.println(scripts);
                    for (String script : scripts) {
                        if (!scriptBlockName.equals("")) {
                            if (script.equals("end::" + scriptBlockName)) {
                                block.add(script);
                                LotteryScript lotteryScript = new LotteryScript(null, scriptBlockName);
                                lotteryScript.parseScript(block, variableMap);
                                scriptList.put(scriptBlockName, lotteryScript);
                                System.out.println(block);
                                scriptBlockName = "";
                                startPushingBlock = false;
                            } else {
                                block.add(script);
                            }
                        } else {
                            if (script.startsWith("::")) {
                                scriptBlockName = script.replace("::", "");
                                block.add(script);
                                startPushingBlock = true;
                                processable.add("block-" + scriptBlockName);
                                System.out.println(scriptBlockName);
                            } else if (script.startsWith("循环")) {
                                String[] splited = script.split(" ");
                                String blockName = splited[1];
                                int times;
                                try {
                                    times = Integer.parseInt(splited[2]);
                                } catch (NumberFormatException e) {
                                    times = Integer.parseInt(ExtraUtils.processStatement(splited[2], variableMap));
                                }
                                scriptList.get(blockName).setLoopTime(times);
                            } else {
                                if (startPushingBlock) {
                                    block.add(script);
                                } else {
                                    processable.add(script);
                                }
                            }
                        }
                    }
                    System.out.println(processable);
                    for(int i = 0; i < processable.size(); i++) {
                        String statement = processable.get(i);
                        if (statement.startsWith("block-")) {
                            processable.set(i, "");
                            //TODO: 添加变量
                            LotteryScript script = scriptList.get(statement.replace("block-", ""));
                            AtomicInteger j = new AtomicInteger(i);
                            script.processBlock(variableMap).forEach(s -> {
                                if (s.startsWith("保底格")) {
                                    processable.add(0, s);
                                } else {
                                    processable.add(j.getAndIncrement() ,s);
                                }
                            });
                        } else {
                            processable.set(i, ExtraUtils.processStatement(statement, variableMap));
                        }
                    }
                    GlobalConfig.GUIScriptList.put(GUIName+"."+scriptName, processable);
                }
            }
            // 运行脚本
        }
    }

    public void runScript(String scriptName,Player player) {
        int delay = 0;
        GlobalConfig.rollingPlayerList.add(player);
        boolean inFloorMode = FloorUtil.hasFloorData(player.getUniqueId());
        List<Integer> floorLists = new ArrayList<>();
        for ( String s : GlobalConfig.GUIScriptList.get(GUIName+"."+scriptName)) {
            if (!s.startsWith("保底格")) break;
            int slot = Integer.parseInt(s.split(" ")[1]);
            floorLists.add(slot);
        }
        for (String script : GlobalConfig.GUIScriptList.get(GUIName+"."+scriptName)) {
            if (delay == 0) {
                if (!script.startsWith("延时")) {
                    if (GlobalConfig.TEMP.get(player.getUniqueId()) == null) {
                        ScriptRunnable runnable = new ScriptRunnable(script, player, pool, itemMap, dataMap, floorLists);
                        runnable.run();
                    }
                } else {
                    int delay2 = Integer.parseInt(script.split(" ")[1]);
                    delay += delay2;
                }
            } else {
                if (!script.startsWith("延时")) {
                    if (GlobalConfig.TEMP.get(player.getUniqueId()) == null) {
                        ScriptRunnable runnable = new ScriptRunnable(script, player, pool, itemMap, dataMap, floorLists);
                    Bukkit.getScheduler().runTaskLater(Lottery.getInstance(), runnable, delay);
                    }
                } else {
                    int delay2 = Integer.parseInt(script.split(" ")[1]);
                    delay += delay2;
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(Lottery.getInstance(), () -> {
            GlobalConfig.rollingPlayerList.remove(player);
            GlobalConfig.TEMP.put(player.getUniqueId(), null);
        }, delay+5L);
    }

    public Inventory inventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void processClickedSlot(int slot, Player player ) {
        String itemName = config.getString("GUI."+slot);
        if (itemName == null) return;
        ConfigurationSection itemSection = config.getConfigurationSection("物品列表").getConfigurationSection(itemName);
        ItemStack itemStack = ExtraUtils.generateItemstackFromYml(itemSection);
        List<String> taskList = config.getStringList("物品列表."+itemName+".功能");
        for ( String task : taskList ) {
            String type = task.split(" ")[0];
            switch (type.toLowerCase()) {
                case "执行脚本":
                    runScript(task.split(" ")[1], player);
                    pool.increaseRollTime(player);
                    break;
                case "刷新玩家奖池数据":
                    pool.refreshPlayerData(player);
                    break;
                case "刷新全服奖池数据":
                    pool.refreshGlobalData(player);
                    break;
                case "消耗前置条件":
                    int times = Integer.parseInt(task.split(" ")[1]);
                    pool.runRequirement(player, times, pool.getDefaultRequirement());
                    break;
                case "直接抽奖":
                    int time = Integer.parseInt(task.split(" ")[1]);
                    for (int i = 0; i < time; i++) {
                        LotteryData data = pool.roll(pool.invalidFilter(player));
                        data.preExecute(player, true);
                        data.execute(player);
                    }
                    break;
                case "转到gui":
                    String GUIName = task.split(" ")[1];
                    LotteryGUI gui = new LotteryGUI(GUIName, player);
                    gui.showInventory(player);
            }

        }
    }

}
