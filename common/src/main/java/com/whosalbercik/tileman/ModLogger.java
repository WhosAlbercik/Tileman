package com.whosalbercik.tileman;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

    public static void sendInfo(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(PREFIX + ChatFormatting.GREEN + message), false);
    }

    public static void sendWarning(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(PREFIX + ChatFormatting.YELLOW + message), false);
    }

    public static void sendError(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(PREFIX + ChatFormatting.RED + message), false);
    }

    public static Component getInfo(String message) {
        return Component.literal(PREFIX + ChatFormatting.GREEN + message);
    }
}
