package com.whosalbercik.tileman.client.renderer;

import com.whosalbercik.tileman.client.TilemanClient;
import com.whosalbercik.tileman.networking.SendSidePanelDataS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

import java.awt.*;

public class SidePanelRenderer {
    private static int availableTiles = 0;
    private static int unlockedTiles = 0;

    public static void render(DrawContext draw) {

        if (TilemanClient.hideSidePanel.isPressed()) return;

        int yMiddle = draw.getScaledWindowHeight() / 2;
        int xMiddle = 14 * (draw.getScaledWindowWidth() / 15);
        int xSize = 90;
        int ySize = 150;

        draw.fill(xMiddle - xSize / 2, yMiddle - ySize / 2, xMiddle + xSize / 2, yMiddle + ySize / 2, new Color(60, 60, 60, 160).hashCode());
        draw.drawText(MinecraftClient.getInstance().textRenderer,
                OrderedText.styledForwardsVisitedString("Tileman", Style.EMPTY.withColor(TextColor.fromRgb(0x00ffff)).withUnderline(true)),
                xMiddle - 18,
                (int) (yMiddle - 0.45 * ySize),
                0xFFFFFF,
                true);

        draw.drawText(MinecraftClient.getInstance().textRenderer,
                OrderedText.styledForwardsVisitedString("Available Tiles: ", Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))),
                xMiddle - 35,
                (int) (yMiddle - 0.3 * ySize),
                0xFFFFFF,
                true);

        draw.drawText(MinecraftClient.getInstance().textRenderer,
                OrderedText.styledForwardsVisitedString(String.valueOf(availableTiles), Style.EMPTY.withColor(availableTiles == 0 ? TextColor.fromRgb(0xFF0000) : TextColor.fromRgb(0x00FF00))),
                xMiddle - 30,
                (int) (yMiddle - 0.22 * ySize),
                0xFFFFFF,
                true);

        draw.drawText(MinecraftClient.getInstance().textRenderer,
                OrderedText.styledForwardsVisitedString("Unlocked Tiles: ", Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))),
                xMiddle - 35,
                (int) (yMiddle - 0.1 * ySize),
                0xFFFFFF,
                true);

        draw.drawText(MinecraftClient.getInstance().textRenderer,
                OrderedText.styledForwardsVisitedString(String.valueOf(unlockedTiles), Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00))),
                xMiddle - 30,
                (int) (yMiddle - 0.2),
                0xFFFFFF,
                true);
    }


    public static void setData(SendSidePanelDataS2C sidePanelData, ClientPlayNetworking.Context context) {
        availableTiles = sidePanelData.availableTiles();
        unlockedTiles = sidePanelData.unlockedTiles();
    }
}
