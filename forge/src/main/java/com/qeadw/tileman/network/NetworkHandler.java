package com.qeadw.tileman.network;

import com.qeadw.tileman.Tileman;
import com.qeadw.tileman.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Tileman.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        // Server to Client packets
        CHANNEL.registerMessage(
            nextId(),
            SendTilesS2CPacket.class,
            SendTilesS2CPacket::encode,
            SendTilesS2CPacket::decode,
            SendTilesS2CPacket::handle
        );

        CHANNEL.registerMessage(
            nextId(),
            ClearRenderedTilesS2CPacket.class,
            ClearRenderedTilesS2CPacket::encode,
            ClearRenderedTilesS2CPacket::decode,
            ClearRenderedTilesS2CPacket::handle
        );

        CHANNEL.registerMessage(
            nextId(),
            SendSidePanelDataS2CPacket.class,
            SendSidePanelDataS2CPacket::encode,
            SendSidePanelDataS2CPacket::decode,
            SendSidePanelDataS2CPacket::handle
        );

        CHANNEL.registerMessage(
            nextId(),
            SendFriendsS2CPacket.class,
            SendFriendsS2CPacket::encode,
            SendFriendsS2CPacket::decode,
            SendFriendsS2CPacket::handle
        );

        // Client to Server packets
        CHANNEL.registerMessage(
            nextId(),
            SetTileAutoClaimC2SPacket.class,
            SetTileAutoClaimC2SPacket::encode,
            SetTileAutoClaimC2SPacket::decode,
            SetTileAutoClaimC2SPacket::handle
        );

        CHANNEL.registerMessage(
            nextId(),
            SendSelectedTileC2SPacket.class,
            SendSelectedTileC2SPacket::encode,
            SendSelectedTileC2SPacket::decode,
            SendSelectedTileC2SPacket::handle
        );

        CHANNEL.registerMessage(
            nextId(),
            ClearSelectedTilesC2SPacket.class,
            ClearSelectedTilesC2SPacket::encode,
            ClearSelectedTilesC2SPacket::decode,
            ClearSelectedTilesC2SPacket::handle
        );
    }
}
