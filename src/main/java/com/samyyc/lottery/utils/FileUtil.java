package com.samyyc.lottery.utils;

import com.samyyc.lottery.Lottery;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileUtil {

    public static List<String> getAllFile(int page, String dirName) {
        File dir = new File(Lottery.getInstance().getDataFolder(), dirName);
        List<File> fileList = Arrays.asList(dir.listFiles());
        List<String> fileName = new ArrayList<>();
        fileList.forEach(file -> {
            fileName.add(file.getName().replace(".yml", ""));
        });
        return fileName.subList(page*10, (page+1)*10);
    }

    public static List<String> getAllPool(int page) {
        return getAllFile(page, "奖池");
    }

    public static List<String> getAllReward(int page) {
        return getAllFile(page, "奖品");
    }

    public static List<String> getAllRewardGroup(int page) {
        return getAllFile(page, "奖品组");
    }

}
