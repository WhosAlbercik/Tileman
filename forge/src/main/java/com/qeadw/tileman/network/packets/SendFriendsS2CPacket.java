package com.qeadw.tileman.network.packets;

import com.qeadw.tileman.client.renderer.BorderRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendFriendsS2CPacket {
    private final String friends;

    public SendFriendsS2CPacket(String friends) {
        this.friends = friends;
    }

    public static void encode(SendFriendsS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.friends, 1000);
    }

    public static SendFriendsS2CPacket decode(FriendlyByteBuf buf) {
        return new SendFriendsS2CPacket(buf.readUtf(1000));
    }

    public static void handle(SendFriendsS2CPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BorderRenderer.setFriends(packet.friends));
        });
        ctx.get().setPacketHandled(true);
    }
}
