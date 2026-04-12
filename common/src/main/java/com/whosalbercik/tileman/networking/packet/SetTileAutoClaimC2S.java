package com.whosalbercik.tileman.networking.packet;

import com.whosalbercik.tileman.networking.Packet;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.rmi.NotBoundException;

/**
 * Sends to server if client wants to enable or disable tile claiming
 */
public class SetTileAutoClaimC2S extends Packet {
    /**
     * Encodes and decodes a {@link Boolean}
     */
    public static final StreamCodec<FriendlyByteBuf, SetTileAutoClaimC2S> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, SetTileAutoClaimC2S::getAutoClaimEnabled, SetTileAutoClaimC2S::new);
    public static final ResourceLocation ID = ResourceLocation.tryBuild("tileman", "set_autoclaim");
    public static final Type<SetTileAutoClaimC2S> TYPE = new Type<>(ID);

    public final boolean autoClaimEnabled;

    public SetTileAutoClaimC2S(boolean autoClaimEnabled) {
        this.autoClaimEnabled = autoClaimEnabled;
    }

    public boolean getAutoClaimEnabled() {
        return autoClaimEnabled;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void serverHandle(ServerPlayer origin) throws NotBoundException {
        PlayerDataHandler.setAutoClaim(autoClaimEnabled, origin);
    }
}
