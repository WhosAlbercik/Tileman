package com.whosalbercik.tileman.networking.packet;

import com.whosalbercik.tileman.LoaderServices;
import com.whosalbercik.tileman.client.ClientTileHandler;
import com.whosalbercik.tileman.networking.Packet;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashSet;


/**
 * Sends tiles to client. This is needed so client can render tile borders in the world
 */
public class SendTilesS2C extends Packet {
    /**
     * Encodes and decodes a {@link HashSet} of {@link OwnedTile}
     */
    public static final StreamCodec<FriendlyByteBuf, SendTilesS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    HashSet::new,
                    OwnedTile.STREAM_CODEC
            ), SendTilesS2C::getTiles,
            SendTilesS2C::new
    );
    public static final ResourceLocation ID = ResourceLocation.tryBuild("tileman", "send_tiles");
    public static final Type<SendTilesS2C> TYPE = new Type<SendTilesS2C>(ID);

    public final Collection<OwnedTile> tiles;

    public SendTilesS2C(Collection<OwnedTile> tiles) {
        this.tiles = tiles;
    }

    public SendTilesS2C(OwnedTile tile) {
        this.tiles = new HashSet<>();
        this.tiles.add(tile);
    }

    public Collection<OwnedTile> getTiles() {
        return tiles;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void clientHandle() {
        HashSet<OwnedTile> rebuilt = new HashSet<>();

        tiles.forEach((tile -> {
            ClientTileHandler.putTile(tile);

            rebuilt.add(tile);
            rebuilt.add(ClientTileHandler.getTile(tile.north().getGlobalPos()));
            rebuilt.add(ClientTileHandler.getTile(tile.south().getGlobalPos()));
            rebuilt.add(ClientTileHandler.getTile(tile.east().getGlobalPos()));
            rebuilt.add(ClientTileHandler.getTile(tile.west().getGlobalPos()));
        }));

        rebuilt.forEach(ClientTileHandler::rebuildTile);
    }
}


