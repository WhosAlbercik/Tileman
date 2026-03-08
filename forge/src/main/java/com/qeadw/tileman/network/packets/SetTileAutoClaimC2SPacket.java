package com.qeadw.tileman.network.packets;

import com.qeadw.tileman.server.PlayerDataHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetTileAutoClaimC2SPacket {
    private final boolean autoClaimEnabled;

    public SetTileAutoClaimC2SPacket(boolean autoClaimEnabled) {
        this.autoClaimEnabled = autoClaimEnabled;
    }

    public static void encode(SetTileAutoClaimC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.autoClaimEnabled);
    }

    public static SetTileAutoClaimC2SPacket decode(FriendlyByteBuf buf) {
        return new SetTileAutoClaimC2SPacket(buf.readBoolean());
    }

    public static void handle(SetTileAutoClaimC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PlayerDataHandler.setAutoClaim(player, packet.autoClaimEnabled);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
