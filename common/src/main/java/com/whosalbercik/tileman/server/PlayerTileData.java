package com.whosalbercik.tileman.server;

import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.UUID;

/**
 * Holds information related to players
 */
public class PlayerTileData {
    /**
     * How much tiles a player can unlock
     */
    public int availableTiles;
    /**
     * Friends of a player
     */
    public HashSet<UUID> friends;
    /**
     * The last safe tile is the tile the player will be pushed back to when unable to unlock a new tile
     */
    public Long lastSafeTile; // do not save this to persistantState as its not necessary
    /**
     * Dimension of the lastSafeTile
     */
    public ResourceKey<Level> lastSafeDimension; // do not save this to persistantState as its not necessary
    /**
     * Selected tiles by player
     */
    public HashSet<OwnedTile> selectedTiles; // do not save this to persistantState as its not necessary
    /**
     * If player has tile claiming enabled
     */
    public boolean autoClaimEnabled = true; // do not save this to persistantState as its not necessary

    /**
     * last time at which player unlocked tile, used for easy mode
     */
    // do not save this
    public long lastTimeClaimed;


    protected PlayerTileData(int availableTiles, HashSet<UUID> friends, Long lastSafeTile, ResourceKey<Level> lastSafeDimension) {
        this.availableTiles = availableTiles;
        this.friends = friends;
        this.lastSafeTile = lastSafeTile;
        this.lastSafeDimension = lastSafeDimension;
        this.selectedTiles = new HashSet<>();
        this.lastTimeClaimed = System.currentTimeMillis();
    }

    protected static PlayerTileData getEmpty(ServerPlayer p) {
        return new PlayerTileData(0, new HashSet<>(), p.blockPosition().asLong(), p.level().dimension());
    }
}
