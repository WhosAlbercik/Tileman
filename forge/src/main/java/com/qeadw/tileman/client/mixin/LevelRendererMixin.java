package com.qeadw.tileman.client.mixin;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "lambda$addMainPass$1", at= @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    public void addMainPass(FogParameters p_365435_, DeltaTracker p_365046_, Camera p_364769_, ProfilerFiller p_369478_, Matrix4f p_361439_, Matrix4f p_369924_, ResourceHandle resourcehandle2, ResourceHandle resourcehandle, ResourceHandle resourcehandle3, ResourceHandle resourcehandle4, Frustum p_363733_, boolean p_362593_, ResourceHandle resourcehandle1, CallbackInfo ci, float f, Vec3 vec3, double d0, double d1, double d2, TextureAtlas atlas, PoseStack posestack, MultiBufferSource.BufferSource multibuffersource$buffersource) {
        BorderRenderer.render(posestack, multibuffersource$buffersource.getBuffer(RenderType.debugQuads()));
    }
}
