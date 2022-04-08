package com.samyyc.lottery.apis;

public class APIManager {

    public void registerAdvLotteryTask(ILotteryTask task) {
        String identifier = task.getIdentifier();
        APIContainer.taskMap.put(identifier, task);
    }

    public void registerReward(ILotteryReward reward) {
        if (reward.needToPutInInventory()) {
            APIContainer.rewardMap.put(reward.getIdentifier(), reward);
        } else {
            APIContainer.preRewardMap.put(reward.getIdentifier(), reward);
        }
    }

}
