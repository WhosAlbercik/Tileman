package com.whosalbercik.tileman.client;


import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import com.whosalbercik.tileman.client.renderer.BorderStyle;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * The type Client tile handler.
 */
public class ClientTileHandler {

    /**
     * The constant allTiles.
     */
    protected static final HashMap<GlobalPos, OwnedTile> allTiles = new HashMap<>();
    /**
     * The constant friends.
     */
    protected static HashSet<UUID> friends = new HashSet<>();
    /**
     * The constant isDirty.
     */
    protected static boolean isDirty = true;

    /**
     * Sets dirty.
     */
    public static void setDirty() {
        isDirty = true;
    }

    /**
     * Sets friends.
     *
     * @param friends the friends
     */
    public static void setFriends(HashSet<UUID> friends) {
        ClientTileHandler.friends = friends;
    }

    /**
     * Gets tile.
     *
     * @param x         the x
     * @param z         the z
     * @param dimension the dimension
     * @return the tile
     */
    public static OwnedTile getTile(int x, int z, ResourceKey<Level> dimension) {
        return allTiles.get(new GlobalPos(dimension, new BlockPos(x, 0, z)));
    }

    /**
     * Gets tile.
     *
     * @param pos the pos
     * @return the tile
     */
    public static OwnedTile getTile(GlobalPos pos) {
        return allTiles.get(pos);
    }

    /**
     * Put tile.
     *
     * @param tile the tile
     */
    public static void putTile(OwnedTile tile) {
        allTiles.put(tile.getGlobalPos(), tile);
    }

    /**
     * Is dirty boolean.
     *
     * @return the boolean
     */
    public static boolean isDirty() {
        return isDirty;
    }

    /**
     * Clear all.
     */
    public static void clearAll() {
        allTiles.clear();
    }

    private static boolean isFriendly(OwnedTile tile) {
        LocalPlayer p = Minecraft.getInstance().player;
        return tile.isOwner(p.getUUID()) || friends.contains(tile.getOwner());
    }

    /**
     * Rebuild all.
     */
    public static void rebuildAll() {
        BorderRenderer.clearTiles();

        for (OwnedTile tile: allTiles.values()) {
            rebuildTile(tile);
        }

        isDirty = false;
    }

    /**
     * Rebuild tile.
     *
     * @param tile the tile
     */
    public static void rebuildTile(OwnedTile tile) {
        if (tile == null) return;

        Profiler.get().push("rebuildTile");

        Profiler.get().pop();

        BorderStyle tileStyle = new BorderStyle(isFriendly(tile) ? ClientConfig.getFriendlyBorder() : ClientConfig.getEnemyBorder(), tile);

        // if the west tile doesnt exist or doesnt have the same friendly status
        if (allTiles.get(tile.west().getGlobalPos()) == null || isFriendly(allTiles.get(tile.west().getGlobalPos())) != isFriendly(tile)) {
            tileStyle.west = true;
        }
        if (allTiles.get(tile.east().getGlobalPos()) == null || isFriendly(allTiles.get(tile.east().getGlobalPos())) != isFriendly(tile)) {
            tileStyle.east = true;
        }
        if (allTiles.get(tile.north().getGlobalPos()) == null || isFriendly(allTiles.get(tile.north().getGlobalPos())) != isFriendly(tile)) {
            tileStyle.north = true;
        }
        if (allTiles.get(tile.south().getGlobalPos()) == null || isFriendly(allTiles.get(tile.south().getGlobalPos())) != isFriendly(tile)) {
            tileStyle.south = true;
        }

        if (tileStyle.isRenderable()) {
            BorderRenderer.putTile(tile, tileStyle);
        } else {
            BorderRenderer.removeTile(tile);
        }

        Profiler.get().pop();
    }

}
