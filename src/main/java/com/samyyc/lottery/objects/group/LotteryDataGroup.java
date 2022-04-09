package com.samyyc.lottery.objects.group;

import com.samyyc.lottery.Lottery;
import com.samyyc.lottery.objects.LotteryData;
import com.samyyc.lottery.objects.LotteryReward;
import com.samyyc.lottery.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class LotteryDataGroup extends LotteryData {

    private List<LotteryReward> rewards = new ArrayList<>();
    private File file;
    private YamlConfiguration config;
    private String name;

    private ConfigurationSection poolSection;
    private ConfigurationSection dataSection;

    private LotteryReward reward;
    private boolean hasLore;

    public LotteryDataGroup(String groupName, ConfigurationSection poolSection, ConfigurationSection dataSection) {
        super(groupName, poolSection, dataSection);
        this.poolSection = poolSection;
        this.dataSection = dataSection;
        groupName = groupName.replace("奖品组#","");
        this.name = groupName;
        file = new File(Lottery.getInstance().getDataFolder(), "奖品组\\"+groupName+".yml");
        config = YamlConfiguration.loadConfiguration(file);
        config.getStringList("奖品列表").forEach(rewardName -> {
            rewards.add(new LotteryReward(rewardName));
        });
    }

    public LotteryReward roll() {
        rewards.forEach(LotteryReward::resetLores);
        Random random = new Random();
        int i = random.nextInt(rewards.size());
        this.reward = rewards.get(i);
        hasLore = false;
        return reward;
    }

    @Override
    public ItemStack getDisplayItemStack() {
        ItemStack displayItemStack = null;
        displayItemStack = reward.getDisplayItem();
        List<String> lores = new ArrayList<>(this.getLores());
        System.out.println(displayItemStack.getItemMeta().getLore());
        if (displayItemStack.getItemMeta().getLore().containsAll(lores)) return displayItemStack;
        if (!hasLore && !lores.isEmpty()) {
            for (int i = 0; i < lores.size(); i++) {
                String s = lores.get(i);
                s = super.replaceData(s);
                lores.set(i, s);
            }
            lores.add(TextUtil.convertColor("&a属于奖品组: ")+name);
            ItemMeta im = displayItemStack.getItemMeta();
            if (im.hasLore()) {
                List<String> lore = im.getLore();
                lore.addAll(lores);
                im.setLore(lore);
            } else {
                im.setLore(lores);
            }
            System.out.println(lores);
            displayItemStack.setItemMeta(im);
            hasLore = true;
        }
        return displayItemStack;
    }

    public LotteryData toData(LotteryReward reward) {
        return new LotteryData(reward.getRewardName(), poolSection, dataSection);
    }

    public LotteryData toData() {
        return toData(this.reward);
    }

    public LotteryData getDataByItemStack(ItemStack itemStack) {
        System.out.println("a");
        System.out.println(itemStack);
        for ( LotteryReward reward : rewards ) {
            System.out.println(reward.getDisplayItem());
            if (reward.getDisplayItem().getType().equals(itemStack.getType())
                    &&
                    reward.getDisplayItem().getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName())
                    &&
                    reward.getDisplayItem().getAmount() == itemStack.getAmount()
            )   {
                this.reward = reward;
                return this;
            }
        }
        return null;
    }

    @Override
    public List<String> getLores() {
        return poolSection.getStringList("lores");
    }

    @Override
    public LotteryReward getReward() {
        return reward;
    }

    @Override
    public void preExecute(Player player, boolean needToIncreaseFlooredTime) {
        super.preExecuteForGroup(player, this.reward, needToIncreaseFlooredTime);
    }
}
