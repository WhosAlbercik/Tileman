package com.whosalbercik.tileman.client.mixin;

import com.whosalbercik.tileman.client.screen.TilemanSettingsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixins into the {@link PauseScreen} to add the button for {@link TilemanSettingsScreen}
 */
@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {

    /**
     * Constructor to satisfy the {@link Screen} super class
     */
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    private void addCustomButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(
                Component.literal("Tileman Settings"),
                button -> this.minecraft.setScreen(new TilemanSettingsScreen(this))
        ).bounds(this.width / 7 - 50, this.height / 10, 100, 20).build());
    }
}
