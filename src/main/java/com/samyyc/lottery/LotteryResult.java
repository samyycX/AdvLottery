package com.samyyc.lottery;

import org.bukkit.entity.Player;

public class LotteryResult {

    private Player player;
    private int slot;
    private LotteryReward reward;

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

    @Override
    public String toString() {
        return "LotteryResult{" +
                "player=" + player +
                ", slot=" + slot +
                ", reward=" + reward +
                '}';
    }
}
