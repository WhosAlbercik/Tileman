package com.qeadw.tileman;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tileman");
    private static final String PREFIX = ChatFormatting.GOLD + "[Tileman] " + ChatFormatting.RESET;

    public static void info(String message) {
        LOGGER.info("[Tileman] " + message);
    }

    public static void warn(String message) {
        LOGGER.warn("[Tileman] " + message);
    }

    public static void error(String message) {
        LOGGER.error("[Tileman] " + message);
    }

    public static void sendInfo(Player player, String message) {
        player.sendSystemMessage(Component.literal(PREFIX + ChatFormatting.GREEN + message));
    }

    public static void sendWarning(Player player, String message) {
        player.sendSystemMessage(Component.literal(PREFIX + ChatFormatting.YELLOW + message));
    }

    public static void sendError(Player player, String message) {
        player.sendSystemMessage(Component.literal(PREFIX + ChatFormatting.RED + message));
    }
}
