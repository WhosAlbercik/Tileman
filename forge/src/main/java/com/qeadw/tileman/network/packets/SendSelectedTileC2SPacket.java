package com.qeadw.tileman.network.packets;

import com.qeadw.tileman.server.PlayerDataHandler;
import com.qeadw.tileman.tile.OwnedTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendSelectedTileC2SPacket {
    private final OwnedTile tile;

    public SendSelectedTileC2SPacket(OwnedTile tile) {
        this.tile = tile;
    }

    public static void encode(SendSelectedTileC2SPacket packet, FriendlyByteBuf buf) {
        packet.tile.toBytes(buf);
    }

    public static SendSelectedTileC2SPacket decode(FriendlyByteBuf buf) {
        return new SendSelectedTileC2SPacket(OwnedTile.fromBytes(buf));
    }

    public static void handle(SendSelectedTileC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PlayerDataHandler.addSelectedTile(player, packet.tile);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
