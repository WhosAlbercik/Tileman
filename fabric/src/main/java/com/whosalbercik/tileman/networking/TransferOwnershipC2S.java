package com.whosalbercik.tileman.networking;

import com.whosalbercik.tileman.Tileman;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;



public record TransferOwnershipC2S(OwnedTile tile, String newOwnerName) implements CustomPayload {
    public static final Id<TransferOwnershipC2S> ID = new Id<>(Tileman.transferOwnership);
    public static final PacketCodec<RegistryByteBuf, TransferOwnershipC2S> CODEC = PacketCodec.tuple(
            OwnedTile.PACKET_CODEC, TransferOwnershipC2S::tile,
            PacketCodecs.STRING, TransferOwnershipC2S::newOwnerName,
            TransferOwnershipC2S::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
