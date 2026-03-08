package com.qeadw.tileman.client.renderer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class SidePanelRenderer {
    private static int availableTiles = 0;
    private static int unlockedTiles = 0;

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int yMiddle = screenHeight / 2;
        int xMiddle = 14 * (screenWidth / 15);
        int xSize = 90;
        int ySize = 150;

        // Draw background panel
        int bgColor = new Color(60, 60, 60, 160).getRGB();
        guiGraphics.fill(
            xMiddle - xSize / 2,
            yMiddle - ySize / 2,
            xMiddle + xSize / 2,
            yMiddle + ySize / 2,
            bgColor
        );

        // Title: "Tileman"
        Component title = Component.literal("Tileman")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withBold(true));
        guiGraphics.drawString(
            mc.font,
            title,
            xMiddle - 18,
            (int) (yMiddle - 0.45 * ySize),
            0xFFFFFF,
            true
        );

        // "Available Tiles:" label
        Component availableLabel = Component.literal("Available Tiles:")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
        guiGraphics.drawString(
            mc.font,
            availableLabel,
            xMiddle - 35,
            (int) (yMiddle - 0.3 * ySize),
            0xFFFFFF,
            true
        );

        // Available tiles count (red if 0, green otherwise)
        int availableColor = availableTiles == 0 ? 0xFF0000 : 0x00FF00;
        Component availableCount = Component.literal(String.valueOf(availableTiles))
            .withStyle(Style.EMPTY.withColor(availableColor));
        guiGraphics.drawString(
            mc.font,
            availableCount,
            xMiddle - 30,
            (int) (yMiddle - 0.22 * ySize),
            0xFFFFFF,
            true
        );

        // "Unlocked Tiles:" label
        Component unlockedLabel = Component.literal("Unlocked Tiles:")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
        guiGraphics.drawString(
            mc.font,
            unlockedLabel,
            xMiddle - 35,
            (int) (yMiddle - 0.1 * ySize),
            0xFFFFFF,
            true
        );

        // Unlocked tiles count
        Component unlockedCount = Component.literal(String.valueOf(unlockedTiles))
            .withStyle(Style.EMPTY.withColor(0x00FF00));
        guiGraphics.drawString(
            mc.font,
            unlockedCount,
            xMiddle - 30,
            (int) (yMiddle - 0.2),
            0xFFFFFF,
            true
        );
    }

    public static void setData(int available, int unlocked) {
        availableTiles = available;
        unlockedTiles = unlocked;
    }

    public static int getAvailableTiles() {
        return availableTiles;
    }

    public static int getUnlockedTiles() {
        return unlockedTiles;
    }
}
