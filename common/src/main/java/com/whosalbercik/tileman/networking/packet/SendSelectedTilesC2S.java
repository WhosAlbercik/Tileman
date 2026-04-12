package com.whosalbercik.tileman.networking.packet;

import com.whosalbercik.tileman.networking.Packet;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.rmi.NotBoundException;
import java.util.Collection;
import java.util.HashSet;


/**
 * Sends tiles that a client has selected to the server. All commansd are executed server-side so the server must know which tiles have been selected by which client
 */
public class SendSelectedTilesC2S  extends Packet{
    /**
     * Encodes and Decodes a {@link HashSet} of {@link OwnedTile}
     */
    public static final StreamCodec<FriendlyByteBuf, SendSelectedTilesC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    HashSet::new,
                    OwnedTile.STREAM_CODEC
            ), SendSelectedTilesC2S::getSelectedTiles,
            SendSelectedTilesC2S::new
    );
    public static final ResourceLocation ID = ResourceLocation.tryBuild("tileman", "send_selected_tiles");
    public static final Type<SendSelectedTilesC2S> TYPE = new Type<>(ID);

    /**
     * Selected tiles
     */
    public final HashSet<OwnedTile> selectedTiles;

    public SendSelectedTilesC2S(HashSet<OwnedTile> selectedTiles) {
        this.selectedTiles = selectedTiles;
    }

    public SendSelectedTilesC2S(OwnedTile tile) {
        this.selectedTiles = new HashSet<>();
        this.selectedTiles.add(tile);
    }

    public SendSelectedTilesC2S() {
        this.selectedTiles = new HashSet<>();
    }


    public HashSet<OwnedTile> getSelectedTiles() {
        return selectedTiles;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void serverHandle(ServerPlayer origin){
        PlayerDataHandler.setSelectedTiles(selectedTiles, origin);
    }
}
