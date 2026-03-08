package com.qeadw.tileman.network.packets;

import com.qeadw.tileman.client.renderer.BorderRenderer;
import com.qeadw.tileman.tile.OwnedTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendTilesS2CPacket {
    private final OwnedTile tile;

    public SendTilesS2CPacket(OwnedTile tile) {
        this.tile = tile;
    }

    public static void encode(SendTilesS2CPacket packet, FriendlyByteBuf buf) {
        packet.tile.toBytes(buf);
    }

    public static SendTilesS2CPacket decode(FriendlyByteBuf buf) {
        return new SendTilesS2CPacket(OwnedTile.fromBytes(buf));
    }

    public static void handle(SendTilesS2CPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BorderRenderer.receiveTileFromServer(packet.tile));
        });
        ctx.get().setPacketHandled(true);
    }
}
