package com.samyyc.lottery;

import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.runnables.ScriptRunnable;
import com.samyyc.lottery.utils.ExtraUtils;
import com.samyyc.lottery.utils.TextUtil;
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

public class LotteryGUI {

    private String GUIName;

    private String poolName;

    private YamlConfiguration config;

    private Inventory inventory;

    private LotteryPool pool;

    private Map<String, ItemStack> itemMap = new HashMap<>();

    private Player player;

    public LotteryGUI(String title, Player player) {
        this.pool = new LotteryPool(title, false);
        pool.invalidFilter(player);
        this.poolName = pool.getName();
        this.GUIName = pool.getGUIName();
        this.player = player;
        initialize();
    }

    public LotteryGUI(LotteryPool pool, Player player) {
        this.pool = pool;
        this.poolName = pool.getName();
        this.GUIName = pool.getGUIName();
        this.player = player;
        initialize();
    }

    public void showInventory(Player player) {
        player.openInventory(inventory);
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
                LotteryData data = new LotteryData(pool.getConfig(), poolName, itemName, player);
                inventory.setItem(slot, data.getDisplayItemStack());
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
                    ListIterator<String> listIterator = processable.listIterator();
                    System.out.println(processable);
                    while (listIterator.hasNext()) {
                        String statement = listIterator.next();
                        if (statement.startsWith("block-")) {
                            listIterator.remove();
                            //TODO: 添加变量
                            LotteryScript script = scriptList.get(statement.replace("block-", ""));
                            script.processBlock(variableMap).forEach(listIterator::add);
                        } else {
                            listIterator.set(ExtraUtils.processStatement(statement, variableMap));
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
        System.out.println(GlobalConfig.GUIScriptList.get(GUIName+"."+scriptName));
        for (String script : GlobalConfig.GUIScriptList.get(GUIName+"."+scriptName)) {
            if (delay == 0) {
                if (!script.startsWith("延时")) {
                    ScriptRunnable runnable = new ScriptRunnable(script, player, pool, itemMap);
                    runnable.run();
                } else {
                    int delay2 = Integer.parseInt(script.split(" ")[1]);
                    delay += delay2;
                }
            } else {
                if (!script.startsWith("延时")) {
                    ScriptRunnable runnable = new ScriptRunnable(script, player, pool, itemMap);
                    Bukkit.getScheduler().runTaskLater(Lottery.getInstance(), runnable, delay);
                } else {
                    int delay2 = Integer.parseInt(script.split(" ")[1]);
                    delay += delay2;
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(Lottery.getInstance(), () -> GlobalConfig.rollingPlayerList.remove(player), delay+5L);
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
                    break;
                case "刷新玩家奖池数据":
                    pool.refreshPlayerData(player);
                    break;
                case "刷新全服奖池数据":
                    pool.refreshGlobalData(player);
                    break;
                case "转到gui":
                    String GUIName = task.split(" ")[1];
                    LotteryGUI gui = new LotteryGUI(GUIName, player);
            }


                /*
                String lineName = task.split(" ", 2)[1];
                LotteryLine line = new LotteryLine(lineName, config);
                final int[] times = {0};
                final int[] totalTimes = {0};
                GlobalConfig.rollingPlayerList.add(player);

                List<Integer> countList = new LinkedList<>();
                int count = 0;
                int time = 0;
                for ( int slot2 : line.slots.keySet()) {
                    String type = line.slots.get(slot2);
                    if ("正常".equals(type)) {
                        count++;
                    } else {
                        countList.add(count);
                        count++;
                        time++;
                    }
                }

                final LotteryData[] data2 = new LotteryData[1];

                final int[] finalSlot = {0};



                boolean enable;
                if (task.contains("赠送抽奖")) {
                    enable = true;
                } else {
                    enable = pool.runRequirement(player, time);
                }
                if (!enable) {
                    GlobalConfig.rollingPlayerList.remove(player);
                    return;
                }

                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        LotteryData data = pool.roll();
                        if (totalTimes[0] != line.rollTime) {
                            int originSlot = line.indexedSlots.get(0);
                            times[0] = totalTimes[0] % line.indexedSlots.size();
                            data.initializePlayerData(player);
                            if (inventory.getItem(originSlot) == null || inventory.getItem(originSlot).getType() == Material.AIR) {
                                inventory.setItem(originSlot, data.getDisplayItemStack());
                            } else {
                                HashMap<Integer, ItemStack> itemStackMap = new LinkedHashMap<>();
                                for (int slot : line.indexedSlots) {
                                    ItemStack item = (inventory.getItem(slot) != null) ? inventory.getItem(slot) : new ItemStack(Material.AIR);
                                    itemStackMap.put(slot, item);
                                }
                                data.initializePlayerData(player);
                                itemStackMap = ExtraUtils.insertItem(itemStackMap, data.getDisplayItemStack());
                                for (Map.Entry<Integer, ItemStack> entry : itemStackMap.entrySet()) {
                                    inventory.setItem(entry.getKey(), entry.getValue());
                                }
                            }

                            player.openInventory(inventory);
                            for (Integer finalCount : countList) {
                                if (totalTimes[0] + finalCount == line.rollTime) {
                                    finalSlot[0] = line.indexedSlots.get(finalCount);
                                    data.preProcess(poolName, player);
                                    if (!GlobalConfig.floorList.isEmpty()) {
                                        LotteryResult result = new LotteryResult(player, finalSlot[0], GlobalConfig.floorList.get(player.getName()));
                                        GlobalConfig.resultList.add(result);
                                        inventory.setItem(finalSlot[0], GlobalConfig.floorList.get(player.getName()).getDisplayItem());
                                        GlobalConfig.floorList.remove(player.getName());
                                        countList.remove(finalCount);
                                    } else {
                                        LotteryResult result = new LotteryResult(player, finalSlot[0], data.reward);
                                        GlobalConfig.resultList.add(result);
                                        countList.remove(finalCount);
                                    }
                                }
                            }
                        }
                        totalTimes[0]++;
                        player.playNote(player.getLocation(), Instrument.BELL, Note.flat(1, Note.Tone.G));
                    }
                };
                int x = 0;
                // x(x+1)+1 < N
                while (true) {
                    int n = (int) Math.pow(x, 2) + x + 1;
                    if ( n<line.rollTime ) {
                        x++;
                    } else if (n >line.rollTime){
                        x--;
                        break;
                    } else break;
                }
                List<Integer> periodList = new LinkedList<>();
                int n = 1;
                for ( int i = x; i > 0; i--) {
                    for (int j = 1; j <=x; j++) {
                        if ( 2*n > 8) {
                            periodList.add(1);
                        } else {
                            periodList.add(2*n);
                        }
                    }
                    n++;
                }

                if (periodList.size() < line.rollTime) {
                    int i = periodList.size();
                    while ( i < line.rollTime) {
                        periodList.add(2);
                        i++;
                    }
                }

                periodList.sort(Integer::compareTo);
                int lastPeriod = 0;
                int timess = 0;
                for ( Integer period : periodList ) {
                    period = period + line.eachTime - periodList.get(0);
                    period += lastPeriod;
                    Bukkit.getScheduler().runTaskLater(Lottery.getInstance(), runnable, period);
                    timess++;
                    lastPeriod = period;
                }

                Bukkit.getScheduler().runTaskLater(Lottery.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        player.playNote(player.getLocation(), Instrument.BELL, Note.flat(1, Note.Tone.C));
                        for ( int slot : line.slots.keySet()) {
                            String type = line.slots.get(slot);
                            if ("正常".equals(type)) {
                                inventory.setItem(slot, new ItemStack(Material.BARRIER));
                                player.openInventory(inventory);
                            }
                            GlobalConfig.rollingPlayerList.remove(player);
                        }
                    }
                }, lastPeriod+periodList.get(periodList.size()-1));


                 */
        }
    }
}

