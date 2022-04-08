package com.samyyc.lottery.objects;

import org.bukkit.entity.Player;

public class LotteryResult {

    private final Player player;
    private final int slot;
    private final LotteryData data;

    public LotteryResult(Player player, int slot, LotteryData data) {
        this.player = player;
        this.slot = slot;
        this.data = data;
    }

    public int getSlot() {
        return slot;
    }

    public LotteryData getLotteryData() {
        return data;
    }

    public Player getPlayer() {
        return player;
    }

    public void preExecute() {
        data.preExecute(player);
    }

    public void execute() {
        data.execute(player);
    }

}
