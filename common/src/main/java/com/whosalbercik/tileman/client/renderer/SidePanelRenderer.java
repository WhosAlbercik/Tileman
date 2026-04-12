package com.whosalbercik.tileman.client.renderer;

import com.whosalbercik.tileman.TilemanClientEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.awt.*;

/**
 * The renderer of the SidePanel
 */
public class SidePanelRenderer implements LayeredDraw.Layer {
    /**
     * Both of these variables are received from the server via {@link com.whosalbercik.tileman.networking.packet.SendSidePanelDataS2C}
     * and used to display correct information
     */
    public static int availableTiles = 0;
    public static int unlockedTiles = 0;

    /**
     * Renders the SidePanel onto the screen <p>
     * If SidePanel is hidden via hotkey, does nothing
     *
     * @param guiGraphics  the gui graphics
     * @param deltaTracker the delta tracker
     */
    public static void renderInternal(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {

      if (TilemanClientEvents.hideSidePanel.isDown()) return;

        int yMiddle = guiGraphics.guiHeight() / 2;
        int xMiddle = 14 * (guiGraphics.guiWidth() / 15);
        int xSize = 90;
        int ySize = 150;

        guiGraphics.fill(xMiddle - xSize / 2, yMiddle - ySize / 2, xMiddle + xSize / 2, yMiddle + ySize / 2, new Color(60, 60, 60, 160).hashCode());
        guiGraphics.drawString(Minecraft.getInstance().font, 
               Component.literal("Tileman").setStyle(Style.EMPTY.withUnderlined(true).withColor(0x00ffff)),
                xMiddle - 18,
                (int) (yMiddle - 0.45 * ySize),
                0xFFFFFF,
                true);

        guiGraphics.drawString(Minecraft.getInstance().font, 
                Component.literal("Available Tiles: ").setStyle(Style.EMPTY.withColor(0xFFFFFF)),
                xMiddle - 35,
                (int) (yMiddle - 0.3 * ySize),
                0xFFFFFF,
                true);

        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal(String.valueOf(availableTiles)).withStyle(Style.EMPTY.withColor(availableTiles == 0 ? 0xFF0000 : 0x00FF00)),
                xMiddle - 30,
                (int) (yMiddle - 0.22 * ySize),
                0xFFFFFF,
                true);

        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Unlocked Tiles: ").withStyle(Style.EMPTY.withColor(0xFFFFFF)),
                xMiddle - 35,
                (int) (yMiddle - 0.1 * ySize),
                0xFFFFFF,
                true);

        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal(String.valueOf(unlockedTiles)).withStyle(Style.EMPTY.withColor(0x00FF00)),
                xMiddle - 30,
                (int) (yMiddle - 0.2),
                0xFFFFFF,
                true);
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        SidePanelRenderer.renderInternal(guiGraphics, deltaTracker);
    }
}
