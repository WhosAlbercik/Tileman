package com.whosalbercik.tileman.client.mixin;

import com.whosalbercik.tileman.client.screen.TilemanSettingsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void addCustomButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Tileman Settings"),
                button -> this.client.setScreen(new TilemanSettingsScreen(this))
        ).dimensions(this.width / 7 - 50, this.height / 10, 100, 20).build());
    }
}
