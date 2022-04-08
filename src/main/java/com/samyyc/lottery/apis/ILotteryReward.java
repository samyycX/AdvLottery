package com.samyyc.lottery.apis;

import org.bukkit.entity.Player;

public interface ILotteryReward {

    boolean needToPutInInventory();

    String getIdentifier();

    void run(Player player, String task);

}
