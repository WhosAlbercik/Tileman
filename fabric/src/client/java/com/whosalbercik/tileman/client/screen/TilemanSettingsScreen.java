package com.whosalbercik.tileman.client.screen;

import com.whosalbercik.tileman.client.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.awt.*;

public class TilemanSettingsScreen extends Screen {

    private final Screen parent;
    public TilemanSettingsScreen(Screen parent) {
        super(Text.literal("Tileman Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.client.setScreen(parent)
        ).dimensions(this.width / 2 - 100, 3 * this.height / 4, 200, 20).build());

        this.addDrawableChild(new SliderWidget(this.width / 2 - 100, this.height / 20, 200, 20, Text.of("Border Render Distance: " + ClientConfig.getBorderRenderDistance()), ClientConfig.getBorderRenderDistance()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Border Render Distance: " + (int) Math.round(this.value * 200)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setBorderRenderDistance((int) (this.value * 200));
            }
        });

        this.addDrawable(new TextWidget(this.width / 2 - 100, 2 * this.height / 20, 200, 20, Text.literal("Friendly Tile Colour"), MinecraftClient.getInstance().textRenderer));

        this.addDrawableChild(new SliderWidget(this.width / 2 - 100, 3 * this.height / 20, 200, 20, Text.of("Red: " + ClientConfig.getFriendlyBorder().getRed()), ClientConfig.getEnemyBorder().getRed()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Red: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setFriendlyBorder(new Color((int) (this.value * 255), ClientConfig.getFriendlyBorder().getGreen(), ClientConfig.getFriendlyBorder().getBlue()));
            }
        });

        this.addDrawableChild(new SliderWidget(this.width / 2 - 100, 4 * this.height / 20, 200, 20, Text.of("Green: " + ClientConfig.getFriendlyBorder().getGreen()), ClientConfig.getEnemyBorder().getGreen()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Green: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setFriendlyBorder(new Color(ClientConfig.getFriendlyBorder().getRed(), (int) (this.value * 255), ClientConfig.getFriendlyBorder().getBlue()));
            }
        });

        this.addDrawableChild(new SliderWidget(this.width / 2 - 100, 5 * this.height / 20, 200, 20, Text.of("Blue: " + ClientConfig.getFriendlyBorder().getBlue()), ClientConfig.getFriendlyBorder().getBlue()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Blue: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setFriendlyBorder(new Color(ClientConfig.getFriendlyBorder().getRed(), ClientConfig.getFriendlyBorder().getGreen(), (int) (this.value * 255)));
            }
        });

        this.addDrawable(new TextWidget(this.width / 2 - 100, 6 * this.height / 20, 200, 20, Text.literal("Enemy Tile Color"), MinecraftClient.getInstance().textRenderer));

        this.addDrawableChild(new SliderWidget(this.width / 2 - 100, 7 * this.height / 20, 200, 20, Text.of("Red: " + ClientConfig.getEnemyBorder().getRed()), ClientConfig.getEnemyBorder().getRed()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Red: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setEnemyBorder(new Color((int) (this.value * 255), ClientConfig.getEnemyBorder().getGreen(), ClientConfig.getEnemyBorder().getBlue()));
            }
        });

        this.addDrawableChild(new SliderWidget(this.width / 2 - 100, 8 * this.height / 20, 200, 20, Text.of("Green: " + ClientConfig.getEnemyBorder().getGreen()), ClientConfig.getEnemyBorder().getGreen()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Green: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setEnemyBorder(new Color(ClientConfig.getEnemyBorder().getRed(), (int) (this.value * 255), ClientConfig.getEnemyBorder().getBlue()));
            }
        });

        this.addDrawableChild(new SliderWidget(this.width / 2 - 100, 9 * this.height / 20, 200, 20, Text.of("Blue: " + ClientConfig.getEnemyBorder().getBlue()), ClientConfig.getEnemyBorder().getBlue()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Blue: " + (int) Math.round(this.value * 255)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setEnemyBorder(new Color(ClientConfig.getEnemyBorder().getRed(), ClientConfig.getEnemyBorder().getGreen(), (int) (this.value * 255)));
            }
        });

        return;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

}
