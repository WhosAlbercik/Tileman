package com.whosalbercik.tileman.client.screen;

import com.whosalbercik.tileman.LoaderServices;
import com.whosalbercik.tileman.client.ClientConfig;
import com.whosalbercik.tileman.client.ClientTileHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;


/**
 * Screen in which are all client-side settings for tileman
 */
public class TilemanSettingsScreen extends Screen {

    private final Screen parent;

    /**
     * Instantiates the Screen
     *
     * @param parent the previous screen, used to return to it when this screen closed
     */
    public TilemanSettingsScreen(Screen parent) {
        super(Component.literal("Tileman Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(
                Component.literal("Close"),
                button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 100, 3 * this.height / 4, 200, 20).build());

        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, this.height / 20, 200, 20,
                Component.literal("Border Render Distance: " + ClientConfig.getBorderRenderDistance()), ClientConfig.getBorderRenderDistance() / 200f) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(("Border Render Distance: " + (int) Math.round(this.value * 200))));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setBorderRenderDistance((int) (this.value * 200));
                ClientTileHandler.setDirty();
            }
        });

        this.addRenderableWidget(new StringWidget(this.width / 2 - 100, 2 * this.height / 20, 200, 20, Component.literal("Friendly Tile Colour"), Minecraft.getInstance().font));

        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, 3 * this.height / 20, 200, 20, Component.literal("Red: " + ClientConfig.getFriendlyBorder().getRed()), ClientConfig.getEnemyBorder().getRed() / 255f) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Red: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setFriendlyBorder(new Color((int) (this.value * 255), ClientConfig.getFriendlyBorder().getGreen(), ClientConfig.getFriendlyBorder().getBlue()));
                ClientTileHandler.setDirty();
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, 4 * this.height / 20, 200, 20, Component.literal("Green: " + ClientConfig.getFriendlyBorder().getGreen()), ClientConfig.getFriendlyBorder().getGreen() / 255f) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Green: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setFriendlyBorder(new Color(ClientConfig.getFriendlyBorder().getRed(), (int) (this.value * 255), ClientConfig.getFriendlyBorder().getBlue()));
                ClientTileHandler.setDirty();
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, 5 * this.height / 20, 200, 20, Component.literal("Blue: " + ClientConfig.getFriendlyBorder().getBlue()), ClientConfig.getFriendlyBorder().getBlue() / 255f) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Blue: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setFriendlyBorder(new Color(ClientConfig.getFriendlyBorder().getRed(), ClientConfig.getFriendlyBorder().getGreen(), (int) (this.value * 255)));
                ClientTileHandler.setDirty();
            }
        });

        this.addRenderableWidget(new StringWidget(this.width / 2 - 100, 6 * this.height / 20, 200, 20, Component.literal("Enemy Tile Color"), Minecraft.getInstance().font));

        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, 7 * this.height / 20, 200, 20, Component.literal("Red: " + ClientConfig.getEnemyBorder().getRed()), ClientConfig.getEnemyBorder().getRed() / 255f) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Red: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setEnemyBorder(new Color((int) (this.value * 255), ClientConfig.getEnemyBorder().getGreen(), ClientConfig.getEnemyBorder().getBlue()));
                ClientTileHandler.setDirty();
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, 8 * this.height / 20, 200, 20, Component.literal("Green: " + ClientConfig.getEnemyBorder().getGreen()), ClientConfig.getEnemyBorder().getGreen() / 255f) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Green: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setEnemyBorder(new Color(ClientConfig.getEnemyBorder().getRed(), (int) (this.value * 255), ClientConfig.getEnemyBorder().getBlue()));
                ClientTileHandler.setDirty();
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, 9 * this.height / 20, 200, 20, Component.literal("Blue: " + ClientConfig.getEnemyBorder().getBlue()), ClientConfig.getEnemyBorder().getBlue() / 255f) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Blue: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setEnemyBorder(new Color(ClientConfig.getEnemyBorder().getRed(), ClientConfig.getEnemyBorder().getGreen(), (int) (this.value * 255)));
                ClientTileHandler.setDirty();
            }
        });

        return;
    }

}
