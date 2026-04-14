package com.qeadw.tileman.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderDebug", at= @At(value = "HEAD"))
    public void render(PoseStack poseStack, MultiBufferSource buffer, Camera camera, CallbackInfo ci) {
        BorderRenderer.render(poseStack, buffer.getBuffer(RenderType.debugQuads()));
    }
}
