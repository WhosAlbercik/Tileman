package com.whosalbercik.tileman.client;


import com.mojang.blaze3d.platform.InputConstants;
import com.whosalbercik.tileman.LoaderServices;
import com.whosalbercik.tileman.TilemanClientEvents;
import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import com.whosalbercik.tileman.client.renderer.SidePanelRenderer;
import com.whosalbercik.tileman.networking.Packet;
import com.whosalbercik.tileman.networking.packet.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.glfw.GLFW;

import java.rmi.NotBoundException;


@Environment(EnvType.CLIENT)
public class TilemanClient implements ClientModInitializer {

    private long lastAutoClaimToggleTime = 0;


    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register((ctx) ->
                BorderRenderer.render(ctx.matrixStack(), ctx.consumers().getBuffer(RenderType.debugQuads())));

        HudRenderCallback.EVENT.register(SidePanelRenderer::renderInternal);


        ClientPlayNetworking.registerGlobalReceiver(SendTilesS2C.TYPE, TilemanClient::handle);
        ClientPlayNetworking.registerGlobalReceiver(SendSidePanelDataS2C.TYPE, TilemanClient::handle);
        ClientPlayNetworking.registerGlobalReceiver(SendTilesS2C.TYPE, TilemanClient::handle);
        ClientPlayNetworking.registerGlobalReceiver(SendFriendsS2C.TYPE, TilemanClient::handle);

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            TilemanClientEvents.tickClient();
        });

        ClientPlayConnectionEvents.JOIN.register((listener, sender, minecraft) -> {
            ClientTileHandler.allTiles.clear();;
        });
    }

    private static void handle(Packet packet, ClientPlayNetworking.Context context) {
        try {
            packet.clientHandle();
        } catch (NotBoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
