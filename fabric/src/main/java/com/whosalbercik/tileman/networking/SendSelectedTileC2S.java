package com.whosalbercik.tileman.networking;

import com.whosalbercik.tileman.Tileman;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;


public record SendSelectedTileC2S(OwnedTile tile) implements CustomPayload {
    public static final Id<SendSelectedTileC2S> ID = new Id<>(Tileman.sendSelectedTile);
    public static final PacketCodec<RegistryByteBuf, SendSelectedTileC2S> CODEC = PacketCodec.tuple(
            OwnedTile.PACKET_CODEC, SendSelectedTileC2S::tile,
            SendSelectedTileC2S::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
