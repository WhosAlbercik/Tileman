package com.qeadw.tileman.client.screen;

import com.qeadw.tileman.client.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class TilemanSettingsScreen extends Screen {
    private final Screen parent;

    public TilemanSettingsScreen(Screen parent) {
        super(Component.literal("Tileman Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Close button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(centerX - 100, 3 * this.height / 4, 200, 20).build());

        // Border Render Distance slider
        this.addRenderableWidget(new AbstractSliderButton(
            centerX - 100,
            this.height / 20,
            200, 20,
            Component.literal("Border Render Distance: " + ClientConfig.getBorderRenderDistance()),
            ClientConfig.getBorderRenderDistance() / 200.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Border Render Distance: " + (int) Math.round(this.value * 200.0)));
            }

            @Override
            protected void applyValue() {
                ClientConfig.setBorderRenderDistance((int) (this.value * 200.0));
            }
        });

        // Friendly Tile Colour label
        this.addRenderableWidget(new StringWidget(
            centerX - 100, 2 * this.height / 20, 200, 20,
            Component.literal("Friendly Tile Colour"),
            this.font
        ));

        // Friendly color sliders (R, G, B)
        this.addRenderableWidget(new AbstractSliderButton(
            centerX - 100, 3 * this.height / 20, 200, 20,
            Component.literal("Red: " + ClientConfig.getFriendlyBorder().getRed()),
            ClientConfig.getFriendlyBorder().getRed() / 255.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Red: " + (int) Math.round(this.value * 255.0)));
            }

            @Override
            protected void applyValue() {
                Color current = ClientConfig.getFriendlyBorder();
                ClientConfig.setFriendlyBorder(new Color(
                    (int) (this.value * 255.0),
                    current.getGreen(),
                    current.getBlue()
                ));
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(
            centerX - 100, 4 * this.height / 20, 200, 20,
            Component.literal("Green: " + ClientConfig.getFriendlyBorder().getGreen()),
            ClientConfig.getFriendlyBorder().getGreen() / 255.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Green: " + (int) Math.round(this.value * 255.0)));
            }

            @Override
            protected void applyValue() {
                Color current = ClientConfig.getFriendlyBorder();
                ClientConfig.setFriendlyBorder(new Color(
                    current.getRed(),
                    (int) (this.value * 255.0),
                    current.getBlue()
                ));
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(
            centerX - 100, 5 * this.height / 20, 200, 20,
            Component.literal("Blue: " + ClientConfig.getFriendlyBorder().getBlue()),
            ClientConfig.getFriendlyBorder().getBlue() / 255.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Blue: " + (int) Math.round(this.value * 255.0)));
            }

            @Override
            protected void applyValue() {
                Color current = ClientConfig.getFriendlyBorder();
                ClientConfig.setFriendlyBorder(new Color(
                    current.getRed(),
                    current.getGreen(),
                    (int) (this.value * 255.0)
                ));
            }
        });

        // Enemy Tile Colour label
        this.addRenderableWidget(new StringWidget(
            centerX - 100, 6 * this.height / 20, 200, 20,
            Component.literal("Enemy Tile Colour"),
            this.font
        ));

        // Enemy color sliders (R, G, B)
        this.addRenderableWidget(new AbstractSliderButton(
            centerX - 100, 7 * this.height / 20, 200, 20,
            Component.literal("Red: " + ClientConfig.getEnemyBorder().getRed()),
            ClientConfig.getEnemyBorder().getRed() / 255.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Red: " + (int) Math.round(this.value * 255.0)));
            }

            @Override
            protected void applyValue() {
                Color current = ClientConfig.getEnemyBorder();
                ClientConfig.setEnemyBorder(new Color(
                    (int) (this.value * 255.0),
                    current.getGreen(),
                    current.getBlue()
                ));
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(
            centerX - 100, 8 * this.height / 20, 200, 20,
            Component.literal("Green: " + ClientConfig.getEnemyBorder().getGreen()),
            ClientConfig.getEnemyBorder().getGreen() / 255.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Green: " + (int) Math.round(this.value * 255.0)));
            }

            @Override
            protected void applyValue() {
                Color current = ClientConfig.getEnemyBorder();
                ClientConfig.setEnemyBorder(new Color(
                    current.getRed(),
                    (int) (this.value * 255.0),
                    current.getBlue()
                ));
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(
            centerX - 100, 9 * this.height / 20, 200, 20,
            Component.literal("Blue: " + ClientConfig.getEnemyBorder().getBlue()),
            ClientConfig.getEnemyBorder().getBlue() / 255.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Blue: " + (int) Math.round(this.value * 255.0)));
            }

            @Override
            protected void applyValue() {
                Color current = ClientConfig.getEnemyBorder();
                ClientConfig.setEnemyBorder(new Color(
                    current.getRed(),
                    current.getGreen(),
                    (int) (this.value * 255.0)
                ));
            }
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
