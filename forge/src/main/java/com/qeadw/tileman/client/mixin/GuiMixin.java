package com.qeadw.tileman.client.mixin;

import com.whosalbercik.tileman.client.renderer.SidePanelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow
    @Final
    private LayeredDraw layers;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(Minecraft p_330021_, CallbackInfo ci) {
        this.layers.add(new SidePanelRenderer());
    }

}
