package com.whosalbercik.tileman.networking;

import com.whosalbercik.tileman.Tileman;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SetTileAutoClaimC2S(boolean autoClaimEnabled) implements CustomPayload {
    public static final Id<SetTileAutoClaimC2S> ID = new Id<>(Tileman.setAutoClaim);

    public static final PacketCodec<RegistryByteBuf, SetTileAutoClaimC2S> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, SetTileAutoClaimC2S::autoClaimEnabled,
            SetTileAutoClaimC2S::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
