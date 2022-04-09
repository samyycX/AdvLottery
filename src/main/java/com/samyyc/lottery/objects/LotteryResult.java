package com.samyyc.lottery.objects;

import org.bukkit.entity.Player;

public class LotteryResult {

    private final Player player;
    private final int slot;
    private final LotteryReward reward;

    public LotteryResult(Player player, int slot, LotteryReward reward) {
        this.player = player;
        this.slot = slot;
        this.reward = reward;
    }

    public int getSlot() {
        return slot;
    }

    public LotteryReward getLotteryReward() {
        return reward;
    }

    public Player getPlayer() {
        return player;
    }

    public void execute() {
        reward.execute(player);
    }

}
