package com.whosalbercik.tileman.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Used when a tile has an owner. if used in calculations and when the owner is irrelevant use {@link Tile}
 */
public class OwnedTile extends Tile{
    private UUID owner;

    /**
     * Encodes and decodes all information in a OwnedTile.
     */
    public static StreamCodec<FriendlyByteBuf, OwnedTile> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, OwnedTile>() {
        public @NotNull OwnedTile decode(FriendlyByteBuf byteBuf) {
            FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(byteBuf);


            BlockPos pos = friendlyBuf.readBlockPos();
            ResourceKey<Level> dimension = friendlyBuf.readNullable(ResourceKey.streamCodec(Level.OVERWORLD.registryKey()));

            String ownersString = friendlyBuf.readUtf();


            return new OwnedTile(pos.getX(), pos.getZ(), dimension, UUID.fromString(ownersString));
        }

        public void encode(FriendlyByteBuf byteBuf, OwnedTile ownedTile) {
            FriendlyByteBuf packetByteBuf = new FriendlyByteBuf(byteBuf);

            packetByteBuf = packetByteBuf.writeBlockPos(new BlockPos(ownedTile.getX(), 0, ownedTile.getZ()));
            packetByteBuf.writeNullable(ownedTile.getDimension(), ResourceKey.streamCodec(Level.OVERWORLD.registryKey()));

            packetByteBuf.writeUtf(ownedTile.owner.toString());
        }
    };


    /**
     * Instantiates a new Owned tile.
     *
     * @param x         the x
     * @param z         the z
     * @param dimension the dimension
     * @param owner     the owner
     */
    protected OwnedTile(int x, int z, ResourceKey<Level> dimension, Player owner) {
        super(x, z, dimension);
        this.owner = owner.getUUID();
    }

    /**
     * Instantiates a new Owned tile.
     *
     * @param x         the x
     * @param z         the z
     * @param dimension the dimension
     * @param owner     the owner
     */
    public OwnedTile(int x, int z, ResourceKey<Level> dimension, UUID owner) {
        super(x, z, dimension);
        this.owner = owner;
    }

    /**
     * Instantiates a new Owned tile.
     *
     * @param pos   the pos
     * @param owner the owner
     */
    public OwnedTile(GlobalPos pos, Player owner) {
        super(pos);
        this.owner = owner.getUUID();
    }

    /**
     * Gets if player is the owner. Being a friend of the owner does not grant ownership
     *
     * @param player the player
     * @return the boolean
     */
    public boolean isOwner(UUID player) {
        return owner.equals(player);
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * Transfer ownership.
     *
     * @param newOwner the new owner
     */
    public void transferOwnership(UUID newOwner) {
        this.owner = newOwner;
    }
}
