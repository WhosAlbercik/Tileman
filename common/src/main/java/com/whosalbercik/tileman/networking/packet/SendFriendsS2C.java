package com.whosalbercik.tileman.networking.packet;

import com.whosalbercik.tileman.client.ClientTileHandler;
import com.whosalbercik.tileman.networking.Packet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.rmi.NotBoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;


/**
 * Packet that sends clients friends to them. This is to ensure that friends tiles are displayed in the friendly border colour and connects to other friendly or owned tiles
 */
public class SendFriendsS2C extends Packet {
    /**
     * Encodes and decodes a {@link HashSet} of {@link UUID}
     */
    public static final StreamCodec<FriendlyByteBuf, SendFriendsS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    HashSet::new,           // factory that creates the collection
                    UUIDUtil.STREAM_CODEC,  // codec for each element
                    256                     // max size
            ), SendFriendsS2C::getFriends,
            SendFriendsS2C::new
    );
    public static final ResourceLocation ID = ResourceLocation.tryBuild("tileman", "send_friends");
    public static final Type<SendFriendsS2C> TYPE = new Type<>(ID);

    /**
     * Clients Friends
     */
    protected HashSet<UUID> friends;

    public SendFriendsS2C(HashSet<UUID> friends) {
        this.friends = friends;
    }

    public HashSet<UUID> getFriends() {
        return friends;
    }


    @Override
    public void clientHandle() throws NotBoundException {
        ClientTileHandler.setFriends(friends);
        ClientTileHandler.setDirty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
