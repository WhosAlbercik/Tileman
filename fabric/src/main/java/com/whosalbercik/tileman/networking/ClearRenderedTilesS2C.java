package com.whosalbercik.tileman.networking;

import com.whosalbercik.tileman.Tileman;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ClearRenderedTilesS2C() implements CustomPayload {
    public static final CustomPayload.Id<ClearRenderedTilesS2C> ID = new CustomPayload.Id<>(Tileman.clearRenderedTiles);

    public static final PacketCodec<RegistryByteBuf, ClearRenderedTilesS2C> CODEC = new PacketCodec<RegistryByteBuf, ClearRenderedTilesS2C>() {
        @Override
        public ClearRenderedTilesS2C decode(RegistryByteBuf buf) {
            return new ClearRenderedTilesS2C();
        }

        @Override
        public void encode(RegistryByteBuf buf, ClearRenderedTilesS2C value) {

        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
