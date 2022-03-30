package com.samyyc.lottery.commands;

import com.samyyc.lottery.enums.Permission;
import org.bukkit.command.CommandSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandCondition {
    int requiredArgLength() default 0;
    String identifier();
    String secondIdentifier() default "";
    Class<? extends CommandSender> senderType() default CommandSender.class;
    Permission requiredPerms() default Permission.PERM_DEFAULT;
}
