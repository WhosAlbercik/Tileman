package com.whosalbercik.tileman;


import com.whosalbercik.tileman.networking.Packet;
import com.whosalbercik.tileman.networking.packet.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;

import java.rmi.NotBoundException;


public class Tileman implements ModInitializer {

    static {
        LoaderServices.PLATFORM = new FabricPlatform();
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(SendTilesS2C.TYPE, SendTilesS2C.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SendSidePanelDataS2C.TYPE, SendSidePanelDataS2C.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SendFriendsS2C.TYPE, SendFriendsS2C.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(SetTileAutoClaimC2S.TYPE, SetTileAutoClaimC2S.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetTileAutoClaimC2S.TYPE, Tileman::handle);

        PayloadTypeRegistry.playC2S().register(SendSelectedTilesC2S.TYPE, SendSelectedTilesC2S.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SendSelectedTilesC2S.TYPE, Tileman::handle);

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> TilemanCommands.registerCommands(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, sender, server) -> {

            if (serverPlayNetworkHandler.player.level().isClientSide) return;

            ServerPlayer p = serverPlayNetworkHandler.getPlayer();

            TilemanEvents.serverSideJoin(p);
        });


        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                TilemanEvents.tickPlayer(player);
            }
        });

        // Give player an available tile when an entity has been killed
        ServerLivingEntityEvents.AFTER_DEATH.register(TilemanEvents::livingEntityDead);

    }

    private static void handle(Packet packet, ServerPlayNetworking.Context context) {
        try {
            packet.serverHandle(context.player());
        } catch (NotBoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
