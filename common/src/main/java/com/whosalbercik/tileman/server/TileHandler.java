package com.whosalbercik.tileman.server;

import com.whosalbercik.tileman.LoaderServices;
import com.whosalbercik.tileman.exception.TileAlreadyUnlockedException;
import com.whosalbercik.tileman.networking.packet.SendSidePanelDataS2C;
import com.whosalbercik.tileman.networking.packet.SendTilesS2C;
import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Saves all unlocked tiles and information about them
 */
public class TileHandler extends SavedData {
    private final HashMap<GlobalPos, OwnedTile> tiles = new HashMap<>();

    // if easy mode is on or not
    public boolean easyMode;


    /**
     * Gets {@link Tile} and information about it
     *
     * @param server    the server
     * @param x         the x coordinate
     * @param z         the z coordinate
     * @param dimension the dimension
     * @return the tile
     */
    public static Tile getTile(MinecraftServer server, int x, int z, ResourceKey<Level> dimension) {
        TileHandler handler = getServerState(server);

        OwnedTile tile = handler.tiles.get(new GlobalPos(dimension, new BlockPos(x, 0, z)));

        return tile == null ? new Tile(x, z, dimension) : tile;
    }

    /**
     * Gets tiles owned by player
     *
     * @param owner the player
     * @return the owned tiles
     */
    public static ArrayList<OwnedTile> getOwnedTiles(ServerPlayer owner) {
        TileHandler handler = getServerState(owner.getServer());

        ArrayList<OwnedTile> owned = new ArrayList<>();

        for (OwnedTile tile: handler.tiles.values()) {
            if (tile.getOwner().equals(owner.getUUID())) {
                owned.add(tile);
            }
        }

        return owned;
    }

    /**
     * Gets owned or friendly tiles. Friendly are tiles owned by friends
     *
     * @param player the player
     * @return the owned and friendly tiles
     */
    public static HashMap<GlobalPos, OwnedTile> getOwnedOrFriendlyTiles(ServerPlayer player) {
        TileHandler handler = getServerState(player.getServer());

        HashMap<GlobalPos, OwnedTile> owned = new HashMap<>();

        for (OwnedTile tile: handler.tiles.values()) {
            if (tile.getOwner().equals(player.getUUID()) || PlayerDataHandler.isFriends(player, tile.getOwner())) {
                owned.put(tile.getGlobalPos(), tile);
            }
        }

        return owned;
    }

    /**
     * If pos is a safe spawnpoint. Check if tiles are unlocked in a 3x3 area
     *
     * @param pos world position
     * @param server the server
     * @return safe or not
     */
    public static boolean isSafeSpawnPoint(GlobalPos pos, MinecraftServer server) {
        HashMap<GlobalPos, OwnedTile> tiles = getServerState(server).tiles;
        BlockPos bPos = new BlockPos(pos.pos().getX(), 0, pos.pos().getZ());

        return !tiles.containsKey(pos) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.offset(1, 0, 0))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.offset(1, 0, 1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.offset(0, 0, 1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.offset(-1, 0, 1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.offset(-1, 0, 0))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.offset(-1, 0, -1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.offset(0, 0, -1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.offset(1, 0, -1)));
    }

    /**
     * Gets safe spawn point for player.
     *
     * @param p the player
     * @return the safe spawn point
     */
    public static BlockPos getSafeSpawnPoint(ServerPlayer p) {
        BlockPos spawn = null;

        for (int z = p.chunkPosition().z; z >= p.chunkPosition().z; z++) {
             spawn = PlayerRespawnLogic.getSpawnPosInChunk(p.serverLevel(), new ChunkPos(p.chunkPosition().x, z));

             // found safe spawnpoint
            if (isSafeSpawnPoint(new GlobalPos(p.level().dimension(), spawn), p.getServer())) {
                return spawn;
            }
        }

        return spawn;
    }

    /**
     * Unlocks tile
     *
     * @param player the player
     * @param x      the x
     * @param z      the z
     * @param world  the world
     * @return the owned tile
     * @throws TileAlreadyUnlockedException If tile is already unlocked. See exception class for details
     */
    public static OwnedTile unlockTile(ServerPlayer player, int x, int z, ResourceKey<Level> world) throws TileAlreadyUnlockedException {
        TileHandler handler = getServerState(player.getServer());
        GlobalPos pos = new GlobalPos(world, new BlockPos(x, 0, z));

        if (handler.tiles.get(pos) != null) {
            throw new TileAlreadyUnlockedException();
        }

        OwnedTile tile = new OwnedTile(pos, player);

        handler.tiles.put(pos, tile);
        handler.setDirty();

        player.getServer().getPlayerList().getPlayers().forEach((srvpl) -> {
            LoaderServices.PLATFORM.sendS2C(srvpl, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(srvpl), getOwnedOrFriendlyTiles(srvpl).size()));
            LoaderServices.PLATFORM.sendS2C(srvpl, new SendTilesS2C(tile));
        });

        return tile;
    }

    /**
     * Unlock 3x3 area for player as spawn point
     *
     * @param p     the player
     * @param pos   the position
     * @param world the world
     */
    public static void unlockStartingSquare(ServerPlayer p, BlockPos pos, ResourceKey<Level> world) {
        // unlock starter tiles
        TileHandler.unlockTile(p, pos.getX(), pos.getZ(), world);
        TileHandler.unlockTile(p,pos.getX() + 1, pos.getZ() - 1, world);
        TileHandler.unlockTile(p,pos.getX() + 1, pos.getZ(), world);
        TileHandler.unlockTile(p,pos.getX() + 1, pos.getZ() + 1, world);
        TileHandler.unlockTile(p,pos.getX(), pos.getZ() - 1, world);
        TileHandler.unlockTile(p,pos.getX(), pos.getZ() + 1, world);
        TileHandler.unlockTile(p,pos.getX() - 1, pos.getZ() - 1, world);
        TileHandler.unlockTile(p,pos.getX() - 1, pos.getZ(), world);
        TileHandler.unlockTile(p,pos.getX() - 1, pos.getZ() + 1, world);
    }

    /**
     * Sends all tiles to client
     *
     * @param player the client
     */
    public static void sendTiles(ServerPlayer player) {
        TileHandler handler = getServerState(player.getServer());

        LoaderServices.PLATFORM.sendS2C(player, new SendTilesS2C(handler.tiles.values()));
    }

    /**
     * Send a {@link OwnedTile} to all clients
     *
     * @param server the server
     * @param tile   the tile
     */
    public static void sendTile(MinecraftServer server, OwnedTile tile) {
        server.getPlayerList().getPlayers().forEach((srvpl) -> {
            LoaderServices.PLATFORM.sendS2C(srvpl, new SendTilesS2C(tile));
        });
    }


    private static TileHandler getServerState(MinecraftServer server) {
        DimensionDataStorage persistentStateManager = server.getLevel(Level.OVERWORLD).getDataStorage();

        TileHandler state = persistentStateManager.computeIfAbsent(new Factory<TileHandler>(TileHandler::new, TileHandler::createFromNbt, null), "tileman.tiles");

        state.setDirty();

        return state;
    }

    private static TileHandler createFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        TileHandler handler = new TileHandler();

        ListTag tiles = tag.getList("tiles", ListTag.TAG_COMPOUND);

        tiles.forEach((nbtTile) -> {
            CompoundTag tile = (CompoundTag) nbtTile;

            ResourceLocation identifier = ResourceLocation.parse(tile.getString("dimension"));

            ResourceKey<Level> dimension = ResourceKey.create(Level.OVERWORLD.registryKey() ,identifier);

            handler.tiles.put(
                    new GlobalPos(dimension, new BlockPos(tile.getInt("posX"), 0, tile.getInt("posZ"))),
                    new OwnedTile(tile.getInt("posX"), tile.getInt("posZ"), dimension, tile.getUUID("owner")));
        });

        handler.easyMode = tag.getBoolean("easyMode");
        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag tiles = new ListTag();
        this.tiles.values().forEach((tile) -> {
            CompoundTag tileNbt = new CompoundTag();

            tileNbt.putInt("posX", tile.getX());
            tileNbt.putInt("posZ", tile.getZ());
            tileNbt.putString("dimension", tile.getDimension().location().toString());

            tileNbt.putUUID("owner", tile.getOwner());

            tiles.add(tileNbt);
        });

        nbt.put("tiles", tiles);
        nbt.putBoolean("easyMode", this.easyMode);
        return nbt;
    }

    /**
     * Is easy mode enabled
     *
     * @param server the server
     * @return enabled or not
     */
// if players should get one tile to unlock after a full minecraft day of not getting any tiles
    public static boolean isEasyMode(MinecraftServer server) {
        return getServerState(server).easyMode;
    }

    /**
     * Toggle easy mode.
     *
     * @param server the server
     */
    public static void toggleEasyMode(MinecraftServer server) {
        TileHandler handler = TileHandler.getServerState(server);

        handler.easyMode = !handler.easyMode;
        handler.setDirty();
    }

}
