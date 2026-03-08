package com.whosalbercik.tileman.networking;

import com.whosalbercik.tileman.Tileman;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ClearSelectedTilesC2S() implements CustomPayload {
    public static final Id<ClearSelectedTilesC2S> ID = new Id<>(Tileman.clearSelectedTiles);

    public static final PacketCodec<RegistryByteBuf, ClearSelectedTilesC2S> CODEC = new PacketCodec<RegistryByteBuf, ClearSelectedTilesC2S>() {
        @Override
        public ClearSelectedTilesC2S decode(RegistryByteBuf buf) {
            return new ClearSelectedTilesC2S();
        }

        @Override
        public void encode(RegistryByteBuf buf, ClearSelectedTilesC2S value) {

        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
