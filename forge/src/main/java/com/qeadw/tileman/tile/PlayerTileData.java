package com.qeadw.tileman.tile;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerTileData {
    public int availableTiles;
    public ArrayList<UUID> friends;
    public Long lastSafeTile;
    public ResourceKey<Level> lastSafeDimension;
    public ArrayList<OwnedTile> selectedTiles;
    public boolean autoClaimEnabled = true;

    public PlayerTileData(int availableTiles, ArrayList<UUID> friends, Long lastSafeTile, ResourceKey<Level> lastSafeDimension) {
        this.availableTiles = availableTiles;
        this.friends = friends;
        this.lastSafeTile = lastSafeTile;
        this.lastSafeDimension = lastSafeDimension;
        this.selectedTiles = new ArrayList<>();
    }

    public PlayerTileData(int availableTiles, ArrayList<UUID> friends, Long lastSafeTile, ResourceKey<Level> lastSafeDimension, ArrayList<OwnedTile> selectedTiles) {
        this.availableTiles = availableTiles;
        this.friends = friends;
        this.lastSafeTile = lastSafeTile;
        this.lastSafeDimension = lastSafeDimension;
        this.selectedTiles = selectedTiles;
    }
}
