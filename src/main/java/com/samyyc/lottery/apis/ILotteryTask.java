package com.samyyc.lottery.apis;

import com.samyyc.lottery.Lottery;
import org.bukkit.entity.Player;

/**
 * 提供的自定义脚本<br><br>
 * <strong>!!!</strong><br>
 * <strong>需要手动使用</strong>
 * <strong> {@link APIManager#registerAdvLotteryTask(ILotteryTask)} 来注册你写的脚本<br> </strong>
 * <strong> {@link APIManager} 对象可以通过 {@link Lottery#getAPI()} 获取 </strong>
 */
public interface ILotteryTask {
    /**
     * 这是你编写的脚本的标识符<p>
     * 当主程序识别到未知的脚本时，会根据标识符寻找<p>
     * 例:<p>
     *  yml配置内写:
     *  <blockquote>
     *  前置条件:<p>
     *      - 全服广播 测试<p>
     *      - <strong>测试脚本</strong> 10<p>
     *  </blockquote>
     * 则这里的<strong>测试脚本</strong>就是你的标识符<p>
     * @return 标识符
     */
    String getIdentifier();

    /**
     * 预检查<br>
     * <br>
     * 当你的程序需要进行一次提前检查时(例如检查玩家有没有足够的钱)<br>
     * 重写该方法并返回对应的值<br>
     * 如果不需要检查直接返回true即可<br>
     * <br>
     * 当你在<strong>奖池</strong>中定义自定义方法时，check方法<strong>会被执行</strong><br>
     * 当你在<strong>奖品</strong>中定义奖励结果时，check方法<strong>不会被执行</strong><br>
     * @param player 提供的玩家对象
     * @param task 提供的脚本文本 (例: "测试脚本 10")
     * @param times 你的前置条件被执行的次数， 例如10连抽时times=10
     * @return 检查是否成功
     */
    boolean check(Player player, String task, int times);

    /**
     * 运行
     * 执行结果
     *
     * @param player 提供的玩家对象
     * @param task 提供的脚本文本
     */
    void run(Player player, String task, int times);

}
