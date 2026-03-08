package com.qeadw.tileman.network.packets;

import com.qeadw.tileman.server.PlayerDataHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearSelectedTilesC2SPacket {

    public ClearSelectedTilesC2SPacket() {
    }

    public static void encode(ClearSelectedTilesC2SPacket packet, FriendlyByteBuf buf) {
        // No data to encode
    }

    public static ClearSelectedTilesC2SPacket decode(FriendlyByteBuf buf) {
        return new ClearSelectedTilesC2SPacket();
    }

    public static void handle(ClearSelectedTilesC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PlayerDataHandler.clearSelectedTiles(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
