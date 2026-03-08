package com.whosalbercik.tileman;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tileman");
    private static final String PREFIX = Formatting.GOLD + "[Tileman] " + Formatting.RESET;

    public static void info(String message) {
        LOGGER.info("[Tileman] " + message);
    }

    public static void warn(String message) {
        LOGGER.warn("[Tileman] " + message);
    }

    public static void error(String message) {
        LOGGER.error("[Tileman] " + message);
    }

    public static void sendInfo(PlayerEntity player, String message) {
        player.sendMessage(Text.literal(PREFIX + Formatting.GREEN + message), false);
    }

    public static void sendWarning(PlayerEntity player, String message) {
        player.sendMessage(Text.literal(PREFIX + Formatting.YELLOW + message), false);
    }

    public static void sendError(PlayerEntity player, String message) {
        player.sendMessage(Text.literal(PREFIX + Formatting.RED + message), false);
    }
}
