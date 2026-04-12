package com.qeadw.tileman.network;

import com.mojang.authlib.GameProfile;
import com.whosalbercik.tileman.Platform;
import com.whosalbercik.tileman.networking.Packet;
import com.whosalbercik.tileman.networking.packet.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import java.nio.file.Path;
import java.rmi.NotBoundException;

public class ForgePlatform implements Platform {

    public static final SimpleChannel CHANNEL = ChannelBuilder.named(ResourceLocation.tryBuild("tileman", "networking")).simpleChannel();


    public static void register() {
        CHANNEL.messageBuilder(SendTilesS2C.class)
                .direction(PacketFlow.CLIENTBOUND)
                .codec(SendTilesS2C.STREAM_CODEC)
                .consumerMainThread(ForgePlatform::handle).add()

        .messageBuilder(SendSidePanelDataS2C.class)
                .direction(PacketFlow.CLIENTBOUND)
                .codec(SendSidePanelDataS2C.STREAM_CODEC)
                .consumerMainThread(ForgePlatform::handle).add()

        .messageBuilder(SendFriendsS2C.class)
                .direction(PacketFlow.CLIENTBOUND)
                .codec(SendFriendsS2C.STREAM_CODEC)
                .consumerMainThread(ForgePlatform::handle).add()

        // Client to Server packets
        .messageBuilder(SetTileAutoClaimC2S.class)
                .direction(PacketFlow.SERVERBOUND)
                .codec(SetTileAutoClaimC2S.STREAM_CODEC)
                .consumerMainThread(ForgePlatform::handle).add()

        .messageBuilder(SendSelectedTilesC2S.class)
                .direction(PacketFlow.SERVERBOUND)
                .codec(SendSelectedTilesC2S.STREAM_CODEC)
                .consumerMainThread(ForgePlatform::handle).add();

    }

    private static void handle(Packet packet, CustomPayloadEvent.Context context) {
        try {
            if (context.isClientSide()) packet.clientHandle();
            else packet.serverHandle(context.getSender());

        } catch (NotBoundException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void sendC2S(Packet packet) {
        CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get().resolve("tileman.properties");
    }

    @Override
    public void sendS2C(ServerPlayer client, Packet packet) {
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(client));
    }
}
