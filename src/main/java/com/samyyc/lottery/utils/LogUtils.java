package com.samyyc.lottery.utils;

import com.samyyc.lottery.Lottery;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LogUtils {


    private static int fileCount = 0;
    private static File file;
    private static boolean usingSQL;

    public static void initLogger(boolean useSQL) {
        if (!useSQL) {
            File folder = new File(Lottery.getInstance().getDataFolder(), "日志");
            if (!folder.exists()) {
                folder.mkdir();
            }
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileCount++;
                    }
                }
            } else {
                fileCount = 0;
            }
            file = new File(Lottery.getInstance().getDataFolder(), "日志\\" + fileCount + "-" + System.currentTimeMillis()+".log");

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Bukkit.getLogger().info(Message.ERROR_FILE.getMessage());
            }

        } else {
            //TODO: 增加SQL功能
        }
        usingSQL = useSQL;
    }

    public static void addLog(String text) {
        if (usingSQL) {
            //TODO: 添加SQL功能
        } else {
            try {
                byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Bukkit.getLogger().info(Message.ERROR_FILE.getMessage());
            }
        }
    }

}
