package com.whosalbercik.tileman.networking;

import com.whosalbercik.tileman.Tileman;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;


public record SendSidePanelDataS2C(int availableTiles, int unlockedTiles) implements CustomPayload {
    public static final Id<SendSidePanelDataS2C> ID = new Id<>(Tileman.sendSidePanel);
    public static final PacketCodec<RegistryByteBuf, SendSidePanelDataS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SendSidePanelDataS2C::availableTiles,
            PacketCodecs.INTEGER, SendSidePanelDataS2C::unlockedTiles,
            SendSidePanelDataS2C::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
