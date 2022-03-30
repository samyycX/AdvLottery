package com.samyyc.lottery.utils;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.configs.GlobalConfig;
import com.samyyc.lottery.containers.GuiContainer;
import com.samyyc.lottery.containers.InventoryContainer;
import com.samyyc.lottery.containers.PoolContainer;
import com.samyyc.lottery.containers.RewardContainer;
import com.samyyc.lottery.enums.Message;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtraUtils {



    /**
     * 根据配置文件中的materialID, 自动转换成Material
     * 支持英文ID或数字ID
     * @param materialID 在配置文件中的materialID
     * @return 返回处理后的material, 如果不存在，则返回<strong>屏障方块</strong>以保证不会引起NullPointerException空指针
     */
    public static Material autoGetMaterial(String materialID) {
        Material material;
        if (StringUtils.isNumeric(materialID)) {
            material = Material.getMaterial(Integer.parseInt(materialID));
        } else {
            materialID = materialID.toUpperCase();
            material = Material.getMaterial(materialID);
        }
        if (Objects.isNull(material)) {
            Bukkit.getLogger().info(Message.ERROR_MATERIAL.getMessage()+" "+materialID);
            return Material.BARRIER;
        }
        return material;
    }

    /**
     * 对于手动配置的物品，自动转换成itemstack
     * @param section 配置文件的section, 通常为"物品A.手动配置"
     * @return 转换过后的itemstack
     */
    public static ItemStack generateItemstackFromYml(ConfigurationSection section) {
        ConfigurationSection manualSection = section.getConfigurationSection("手动配置");
        if ( !Objects.isNull(manualSection)) {
            int itemAmount = (manualSection.getInt("amount") == 0) ? 1 : manualSection.getInt("amount");
            String materialID = manualSection.getString("material");
            Material itemMaterial = autoGetMaterial(materialID);
            String itemName = manualSection.getString("name");
            List<String> itemLore = manualSection.getStringList("lore");
            short itemDamage = (short) manualSection.getInt("damage");
            itemName = TextUtil.convertColor(itemName);
            itemLore = TextUtil.convertColor(itemLore);

            ItemStack itemStack = new ItemStack(itemMaterial, itemAmount, itemDamage);
            ItemMeta meta = itemStack.getItemMeta();
            if (!Objects.isNull(itemName)) meta.setDisplayName(itemName);
            if (!Objects.isNull(itemLore)) meta.setLore(itemLore);
            itemStack.setItemMeta(meta);

            return itemStack;
        } else {
            return section.getItemStack("自动配置");
        }

    }

    /**
     * 在玩家背包内检查/删除指定量物品
     * 只要能实现功能就可以,不要纠结看不看得懂
     * @param player 被执行的玩家
     * @param itemStack 物品(本身的amount属性不管用!)
     * @param amount 处理的数量(可以大于一组数量)
     * @param take 是否删除物品
     * @return 检查成功/删除成功
     */
    public static boolean checkItemstackInInventory(Player player, ItemStack itemStack, int amount, boolean take) {
        boolean returnValue;
        itemStack.setAmount(1);
        Inventory inventory = player.getInventory();
        int inventorySize = inventory.getSize();

        Map<Integer, ItemStack> itemStackMap = new LinkedHashMap<>();

        int nowAmount = 0;
        for (int i = 0; i < inventorySize; i++) {
            ItemStack itemstackInInventory = inventory.getItem(i);
            if (itemstackInInventory != null && itemstackInInventory.getType()!=Material.AIR) {
                int inventoryItemstackAmount = itemstackInInventory.getAmount();
                itemstackInInventory.setAmount(1);
                if (itemstackInInventory.equals(itemStack)) {
                    nowAmount = nowAmount + inventoryItemstackAmount;
                    itemstackInInventory.setAmount(inventoryItemstackAmount);
                    itemStackMap.put(i, itemstackInInventory);
                }
                itemstackInInventory.setAmount(inventoryItemstackAmount);
            }
        }
        returnValue =  (nowAmount >= amount);


        if (take) {
            int lastTookAmount = 0;
            int nowTookAmount = 0;
            HashMap<Integer, ItemStack> newItemStackMap = new LinkedHashMap<>();
            for (Map.Entry<Integer, ItemStack> entry : itemStackMap.entrySet()) {
                if ( returnValue ) {
                    ItemStack newItemStack = entry.getValue();
                    int itemAmount = newItemStack.getAmount();
                    nowTookAmount = nowTookAmount + itemAmount;
                    if ( nowTookAmount > amount) {
                        int needToTake = amount - lastTookAmount;
                        newItemStack.setAmount(itemAmount - needToTake);
                        entry.setValue(newItemStack);
                        newItemStackMap.put(entry.getKey(), newItemStack);
                        break;
                    } else if (nowTookAmount == amount) {
                        newItemStack.setType(Material.AIR);
                        entry.setValue(newItemStack);
                        newItemStackMap.put(entry.getKey(), newItemStack);
                        break;
                    } else {
                        lastTookAmount = nowTookAmount;
                        newItemStack.setType(Material.AIR);
                        newItemStackMap.put(entry.getKey(), newItemStack);
                    }
                }
            }
            for (Map.Entry<Integer, ItemStack> entry : newItemStackMap.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }

            player.updateInventory();
        }
        return returnValue;
    }

    /**
     * 给玩家在聊天框提供命令帮助
     * @param player 需要帮助的玩家
     */
    public static void printHelpToPlayer(CommandSender player) {
        List<String> messageList = new ArrayList<>();
        messageList.add("&a===============&e[AdvLottery]&a===============");
        messageList.add("&b/advlottery help &e#显示此帮助");
        messageList.add("&b/advlottery open <奖池名> &e#打开奖池");
        messageList.add("&b/advlottery inventory &e#打开抽奖背包");
        messageList.add("&c以下指令需要OP权限");
        messageList.add("&b/advlottery pool <奖池名> create &e#创建奖池");
        messageList.add("&b/advlottery pool <奖池名> addreward <奖品名> &e#向奖池添加奖品");
        messageList.add("&b/advlottery reward <奖品名> create &e#创建奖品");
        messageList.add("&b/advlottery reward <奖品名> setdisplayitem &e#设置显示物品");
        messageList.add("&b/advlottery reward <奖品名> setitem <物品名> &e#设置物品");
        messageList.add("&b/advlottery reload &e#重载所有配置");
        messageList.add("&a==========================================");
        messageList = TextUtil.convertColor(messageList);
        messageList.forEach(player::sendMessage);

    }

    @Deprecated
    public static HashMap<Integer, ItemStack> insertItem(Map<Integer, ItemStack> map, ItemStack inserted) {
        HashMap<Integer, ItemStack> newMap = new LinkedHashMap<>();
        int i = 0;
        ItemStack lastElement = null;
        for (Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
            if (i == 0) {
                lastElement = entry.getValue();
                entry.setValue(inserted);
            } else {
                ItemStack element = entry.getValue();
                entry.setValue(lastElement);
                lastElement = element;
            }
            newMap.put(entry.getKey(), entry.getValue());
            i++;

        }
        return newMap;
    }

    public static String replaceAll(String text, String regex, Object replacement) {
        if (replacement != null) {
            return text.replaceAll(regex, String.valueOf(replacement));
        } else {
            return text.replaceAll(regex, "");
        }
    }

    @Deprecated
    public static byte[] delete(int index, byte[] array) {
        byte[] arrNew = new byte[array.length - 1];
        if (array.length - 1 - index >= 0) System.arraycopy(array, index + 1, array, index, array.length - 1 - index);
        System.arraycopy(array, 0, arrNew, 0, arrNew.length);
        return arrNew;
    }

    public static String Insert(int start, int end, String text, String original) {
        StringBuilder buffer = new StringBuilder(original);
        buffer.replace(start, end, text);
        return buffer.toString();

    }

    public static String processStatement(String statement, Map<String, String> variableMap) {
        if (!statement.contains("{") && !statement.contains("~")) return statement;

        String temp = statement;
        int amount = temp.length()-temp.replaceAll("\\{","").length();
        temp = temp.replaceAll("\\{","");
        amount = amount + (temp.length() - temp.replaceAll("~","").length())/2;
        temp = null;
        for (int i = 0; i < amount; i++) {
            boolean changed = true;
            boolean changed2 = false;
            while (changed) {
                for (Map.Entry<String, String> entry : variableMap.entrySet()) {
                    if (statement.contains("{" + entry.getKey() + "}")) {
                        statement = statement.replace("{" + entry.getKey() + "}", entry.getValue());
                        changed2 = true;
                    }
                }
                changed = changed2;
                changed2 = false;
            }
            if (statement.contains("~")) {
                Pattern pattern = Pattern.compile("(?<=~)(.+?)(?=~)");
                Matcher matcher = pattern.matcher(statement);
                int matcher_start = 0;
                while (matcher.find(matcher_start)) {
                    String expression = matcher.group(1);
                    System.out.println(expression);
                    try {
                        double result = Calculator.executeExpression(expression);
                        if ((int) result == result) {
                            statement = statement.replace("~" + matcher.group(0) + "~", String.valueOf((int) result));
                        } else {
                            statement = statement.replace("~" + matcher.group(0) + "~", String.valueOf(result));
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                    matcher_start = matcher.end();
                }
            }
            Pattern pattern = Pattern.compile("(?<=取整)((\\d+\\.\\d+)|(\\d+))");
            Matcher matcher = pattern.matcher(statement);
            int matcher_start = 0;
            while (matcher.find(matcher_start)) {
                int result = 0;
                if (matcher.group(2) != null) {
                    result = (int) Double.parseDouble(matcher.group(2));
                } else if (matcher.group(3) != null) {
                    result = Integer.parseInt(matcher.group(3));
                }
                statement = statement.replace("取整"+matcher.group(0), String.valueOf(result));
                matcher_start = matcher.end();
            }
        }

        return statement;

    }

    public static void initConfigurations() {
        Lottery instance = Lottery.getInstance();
        if (!instance.getDataFolder().exists()) {
            instance.getDataFolder().mkdir();

            File folder = new File(instance.getDataFolder(), "GUI页面");
            folder.mkdir();
            instance.saveResource("GUI页面\\example.yml", false);

            folder = new File(instance.getDataFolder(), "奖池");
            folder.mkdir();
            instance.saveResource("奖池\\example.yml", false);

            folder = new File(instance.getDataFolder(), "奖品");
            folder.mkdir();
            instance.saveResource("奖品\\example.yml", false);

            new File(instance.getDataFolder(), "背包").mkdir();
            new File(instance.getDataFolder(), "数据").mkdir();

            instance.saveDefaultConfig();

        }
    }

    public static void destroy() {
        PoolContainer.destroy();
        RewardContainer.destroy();
        GuiContainer.destroy();
        InventoryContainer.destroy();
        GlobalConfig.destroy();
    }
}

