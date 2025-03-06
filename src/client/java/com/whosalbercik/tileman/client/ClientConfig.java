package com.whosalbercik.tileman.client;

import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ClientConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("tileman.properties");
    private static final Properties properties = new Properties();
    private static int borderRenderDistance = 100;

    private static Color friendlyBorder = new Color(255, 0, 0);
    private static Color enemyBorder = new Color(0, 0, 255);


    static {
        loadConfig();
    }

    private static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);
                borderRenderDistance = Integer.parseInt(properties.getProperty("border_render_distance", "100"));
                friendlyBorder = new Color(
                        Integer.parseInt(properties.getProperty("friendly_red", "255")),
                        Integer.parseInt(properties.getProperty("friendly_green", "0")),
                        Integer.parseInt(properties.getProperty("friendly_blue", "0"))
                );

                enemyBorder = new Color(
                        Integer.parseInt(properties.getProperty("enemy_red", "0")),
                        Integer.parseInt(properties.getProperty("enemy_green", "255")),
                        Integer.parseInt(properties.getProperty("enemy_blue", "0"))
                );

            } catch (IOException | NumberFormatException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            }
        }
    }

    public static void saveConfig() {
        try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            properties.setProperty("border_render_distance", String.valueOf(borderRenderDistance));

            properties.setProperty("friendly_red", String.valueOf(friendlyBorder.getRed()));
            properties.setProperty("friendly_green", String.valueOf(friendlyBorder.getGreen()));
            properties.setProperty("friendly_blue", String.valueOf(friendlyBorder.getBlue()));

            properties.setProperty("enemy_red", String.valueOf(enemyBorder.getRed()));
            properties.setProperty("enemy_green", String.valueOf(enemyBorder.getGreen()));
            properties.setProperty("enemy_blue", String.valueOf(enemyBorder.getBlue()));



            properties.store(output, "Tileman Mod Config");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    public static int getBorderRenderDistance() {
        return borderRenderDistance;
    }

    public static void setBorderRenderDistance(int value) {
        borderRenderDistance = value;
        saveConfig();
    }

    public static Color getFriendlyBorder() {
        return friendlyBorder;
    }

    public static void setFriendlyBorder(Color friendlyBorder) {
        ClientConfig.friendlyBorder = friendlyBorder;
        saveConfig();
    }

    public static Color getEnemyBorder() {
        return enemyBorder;
    }

    public static void setEnemyBorder(Color enemyBorder) {
        ClientConfig.enemyBorder = enemyBorder;
        saveConfig();
    }
}
