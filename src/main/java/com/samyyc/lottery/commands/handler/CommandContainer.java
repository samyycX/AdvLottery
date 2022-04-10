package com.samyyc.lottery.commands.handler;

import com.samyyc.lottery.commands.command.*;

import java.util.HashSet;
import java.util.Set;

public class CommandContainer {

    private static Set<Class<?>> commandHandlerSet = new HashSet<>();

    public static void init() {
        commandHandlerSet = getAllCommandHandler("com.samyyc.lottery.commands");
    }

    public static Set<Class<?>> getAllCommandHandler(String packageName) {
        return new HashSet<Class<?>>() {{
            add(GuiCommand.class);
            add(HelpCommand.class);
            add(InventoryCommand.class);
            add(PoolCommand.class);
            add(ReloadCommand.class);
            add(RewardCommand.class);
            // 傻逼了，本来想用反射包，但是bukkit不支持
        }};
    }

    public static Set<Class<?>> getCommandHandlerSet() {
        return commandHandlerSet;
    }

}
