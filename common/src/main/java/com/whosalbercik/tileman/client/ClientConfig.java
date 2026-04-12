package com.whosalbercik.tileman.client;

import com.whosalbercik.tileman.LoaderServices;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;


/**
 * Holds all client-side settings for Tileman
 */
public class ClientConfig {
    private static final Properties properties = new Properties();
    private static int borderRenderDistance = 100;

    private static Color friendlyBorder = new Color(255, 0, 0);
    private static Color enemyBorder = new Color(0, 0, 255);

    private static boolean autoClaimEnabled = true;

    static {
        loadConfig();
    }

    private static void loadConfig() {

        if (Files.exists(LoaderServices.PLATFORM.getConfigDir())) {
            try (InputStream input = Files.newInputStream(LoaderServices.PLATFORM.getConfigDir())) {
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

    private static void saveConfig() {
        try (OutputStream output = Files.newOutputStream(LoaderServices.PLATFORM.getConfigDir())) {
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

    /**
     * Gets border render distance.
     *
     * @return the border render distance
     */
    public static int getBorderRenderDistance() {
        return borderRenderDistance;
    }

    /**
     * Sets border render distance.
     *
     * @param value new render distance
     */
    public static void setBorderRenderDistance(int value) {
        borderRenderDistance = value;
        saveConfig();
    }

    /**
     * Gets the colour of friendly borders
     *
     * @return the friendly border colour
     */
    public static Color getFriendlyBorder() {
        return friendlyBorder;
    }

    /**
     * Sets the colour of friendly borders
     *
     * @param friendlyBorder the new colour
     */
    public static void setFriendlyBorder(Color friendlyBorder) {
        ClientConfig.friendlyBorder = friendlyBorder;
        saveConfig();
    }

    /**
     * Gets colour of enemy borders
     *
     * @return the enemy border colour
     */
    public static Color getEnemyBorder() {
        return enemyBorder;
    }

    /**
     * Sets enemy border colour
     *
     * @param enemyBorder the new enemy border colour
     */
    public static void setEnemyBorder(Color enemyBorder) {
        ClientConfig.enemyBorder = enemyBorder;
        saveConfig();
    }

    /**
     * Changes the client-side variable of if tile-claiming is enabled
     */
    public static void toggleAutoClaim() {
        autoClaimEnabled = !autoClaimEnabled;
    }

    /**
     * Gets if tile-claiming is enabled
     *
     * @return the tile claim enabled
     */
    public static boolean getAutoClaimEnabled() {
        return autoClaimEnabled;
    }
}
