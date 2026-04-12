package com.whosalbercik.tileman;

import com.whosalbercik.tileman.networking.Packet;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public class FabricPlatform implements Platform {
    static {
        KeyBindingHelper.registerKeyBinding(TilemanClientEvents.toggleAutoClaim);
        KeyBindingHelper.registerKeyBinding(TilemanClientEvents.hideSidePanel);
        KeyBindingHelper.registerKeyBinding(TilemanClientEvents.setArea);
    }

    @Override
    public void sendS2C(ServerPlayer client, Packet packet) {
        ServerPlayNetworking.send(client, packet);
    }

    @Override
    public void sendC2S(Packet packet) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("tileman.properties");
    }

}
