package com.samyyc.lottery.miscs;

import com.samyyc.lottery.LotteryPool;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AdvLotteryExpansion extends PlaceholderExpansion {

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "advlottery";
    }

    @Override
    public String getAuthor() {
        return "samyyc";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        // 保底次数
        // %advlottery_玩家已出次数_奖池A_奖品A%
        // %advlottery_玩家已保底次数_奖池A_奖品A%
        // %advlottery_玩家已出保底次数_奖池A_奖品A%
        String[] args = identifier.split("_",3);
        String poolname = args[1];
        String rewardName = args[2];
        LotteryPool lotteryPool = new LotteryPool(poolname, false, player);

        return lotteryPool.getRewardData(player, identifier);
    }
}
