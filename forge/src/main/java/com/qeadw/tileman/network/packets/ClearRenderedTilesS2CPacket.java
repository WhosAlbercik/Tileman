package com.qeadw.tileman.network.packets;

import com.qeadw.tileman.client.renderer.BorderRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearRenderedTilesS2CPacket {

    public ClearRenderedTilesS2CPacket() {
    }

    public static void encode(ClearRenderedTilesS2CPacket packet, FriendlyByteBuf buf) {
        // No data to encode
    }

    public static ClearRenderedTilesS2CPacket decode(FriendlyByteBuf buf) {
        return new ClearRenderedTilesS2CPacket();
    }

    public static void handle(ClearRenderedTilesS2CPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> BorderRenderer::clearRenderedTiles);
        });
        ctx.get().setPacketHandled(true);
    }
}
