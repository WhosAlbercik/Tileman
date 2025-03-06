package com.whosalbercik.tileman.tile;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerTileData {
    public int availableTiles;
    public ArrayList<UUID> friends;
    public Long lastSafeTile; // do not save this to persistantState as its not necessary
    public RegistryKey<World> lastSafeDimension; // do not save this to persistantState as its not necessary


    public PlayerTileData(int availableTiles, ArrayList<UUID> friends, Long lastSafeTile, RegistryKey<World> lastSafeDimension) {
        this.availableTiles = availableTiles;
        this.friends = friends;
        this.lastSafeTile = lastSafeTile;
        this.lastSafeDimension = lastSafeDimension;
    }
}
