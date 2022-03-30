package com.samyyc.lottery.commands;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandContainer {

    private static Set<Class<?>> commandClassesSet = new HashSet<>();

    public static void init() {
        commandClassesSet = getAllCommandClasses("com.samyyc.lottery.commands");
    }

    public static Set<Class<?>> getAllCommandClasses(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getTypesAnnotatedWith(Command.class);
    }

    public static Set<Class<?>> getCommandClassesSet() {
        return commandClassesSet;
    }

}
