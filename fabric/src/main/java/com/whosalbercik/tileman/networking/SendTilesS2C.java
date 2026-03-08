package com.whosalbercik.tileman.networking;

import com.whosalbercik.tileman.Tileman;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;


public record SendTilesS2C(OwnedTile tile) implements CustomPayload {
    public static final CustomPayload.Id<SendTilesS2C> ID = new CustomPayload.Id<>(Tileman.sendTiles);
    public static final PacketCodec<RegistryByteBuf, SendTilesS2C> CODEC = PacketCodec.tuple(
            OwnedTile.PACKET_CODEC, SendTilesS2C::tile,
            SendTilesS2C::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
