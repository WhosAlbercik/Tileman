package com.whosalbercik.tileman.networking;

import com.whosalbercik.tileman.Tileman;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;


public record SendFriendsS2C(String friends) implements CustomPayload {
    public static final Id<SendFriendsS2C> ID = new Id<>(Tileman.sendFriends);
    public static final PacketCodec<RegistryByteBuf, SendFriendsS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.string(1000), SendFriendsS2C::friends,
            SendFriendsS2C::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
