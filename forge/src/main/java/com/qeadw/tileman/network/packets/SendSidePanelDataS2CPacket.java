package com.qeadw.tileman.network.packets;

import com.qeadw.tileman.client.renderer.SidePanelRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendSidePanelDataS2CPacket {
    private final int availableTiles;
    private final int unlockedTiles;

    public SendSidePanelDataS2CPacket(int availableTiles, int unlockedTiles) {
        this.availableTiles = availableTiles;
        this.unlockedTiles = unlockedTiles;
    }

    public static void encode(SendSidePanelDataS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.availableTiles);
        buf.writeInt(packet.unlockedTiles);
    }

    public static SendSidePanelDataS2CPacket decode(FriendlyByteBuf buf) {
        return new SendSidePanelDataS2CPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(SendSidePanelDataS2CPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SidePanelRenderer.setData(packet.availableTiles, packet.unlockedTiles));
        });
        ctx.get().setPacketHandled(true);
    }
}
