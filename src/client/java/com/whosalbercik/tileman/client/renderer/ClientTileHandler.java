package com.whosalbercik.tileman.client.renderer;

import com.whosalbercik.tileman.client.ClientConfig;
import com.whosalbercik.tileman.networking.ClearRenderedTilesS2C;
import com.whosalbercik.tileman.networking.SendFriendsS2C;
import com.whosalbercik.tileman.networking.SendTilesS2C;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientTileHandler {

    private static final HashMap<GlobalPos, OwnedTile> allTiles = new HashMap<>();
    private static ArrayList<UUID> friends = new ArrayList<>();
    protected static boolean isDirty = true;

    public static void setDirty() {
        isDirty = true;
    }

    protected static void rebuildAll() {
        BorderRenderer.clearTiles();

        for (OwnedTile tile: allTiles.values()) {
            rebuildTile(tile);
        }

        isDirty = false;
    }

    public static void rebuildTile(OwnedTile tile) {
        if (tile == null) return;

        Profilers.get().push("rebuildTile");

        Profilers.get().pop();

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

        Profilers.get().pop();
    }
    
    private static boolean isFriendly(OwnedTile tile) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        return tile.isOwner(p.getUuid()) || friends.contains(tile.getOwner());
    }


    public static void reveiveTileFromServer(SendTilesS2C sendTilesS2C, ClientPlayNetworking.Context context) {
        OwnedTile tile = sendTilesS2C.tile();
        allTiles.put(tile.getGlobalPos(), tile);

        rebuildTile(tile);
        rebuildTile(allTiles.get(tile.north().getGlobalPos()));
        rebuildTile(allTiles.get(tile.south().getGlobalPos()));
        rebuildTile(allTiles.get(tile.east().getGlobalPos()));
        rebuildTile(allTiles.get(tile.west().getGlobalPos()));
    }

    public static void clearRenderedTiles(ClearRenderedTilesS2C clearRenderedTilesS2C, ClientPlayNetworking.Context context) {
        allTiles.clear();
        BorderRenderer.clearTiles();
    }

    public static void setFriends(SendFriendsS2C sendFriendsS2C, ClientPlayNetworking.Context context) {
        String friendsString = sendFriendsS2C.friends();
        String[] friendsArray = friendsString.split(";");

        friends = new ArrayList<>();

        if (friendsArray[0].isEmpty()) return;

        for (String friendString: friendsArray) {
            friends.add(UUID.fromString(friendString));
        }

        rebuildAll();
    }

    public static OwnedTile getTile(int x, int z, RegistryKey<World> dimension) {
        return allTiles.get(new GlobalPos(dimension, new BlockPos(x, 0, z)));
    }


}
