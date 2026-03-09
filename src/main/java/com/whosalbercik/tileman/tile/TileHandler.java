package com.whosalbercik.tileman.tile;

import com.whosalbercik.tileman.exception.TileAlreadyUnlockedException;
import com.whosalbercik.tileman.networking.ClearRenderedTilesS2C;
import com.whosalbercik.tileman.networking.SendSidePanelDataS2C;
import com.whosalbercik.tileman.networking.SendTilesS2C;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;

public class TileHandler extends PersistentState {
    public HashMap<GlobalPos, OwnedTile> tiles = new HashMap<>();

    // if easy mode is on or not
    public boolean easyMode;

    private static Type<TileHandler> persistantStateType = new Type<>(
            TileHandler::new,
            TileHandler::createFromNbt,
            null
    );


    public static Tile getTile(MinecraftServer server, int x, int z, RegistryKey<World> dimension) {
        TileHandler handler = getServerState(server);

        OwnedTile tile = handler.tiles.get(new GlobalPos(dimension, new BlockPos(x, 0, z)));

        return tile == null ? new Tile(x, z, dimension) : tile;
    }

    public static ArrayList<OwnedTile> getOwnedTiles(ServerPlayerEntity owner) {
        TileHandler handler = getServerState(owner.getServer());

        ArrayList<OwnedTile> owned = new ArrayList<>();

        for (OwnedTile tile: handler.tiles.values()) {
            if (tile.getOwner().equals(owner.getUuid())) {
                owned.add(tile);
            }
        }

        return owned;
    }

    public static HashMap<GlobalPos, OwnedTile> getOwnedOrFriendlyTiles(ServerPlayerEntity owner) {
        TileHandler handler = getServerState(owner.getServer());

        HashMap<GlobalPos, OwnedTile> owned = new HashMap<>();

        for (OwnedTile tile: handler.tiles.values()) {
            if (tile.getOwner().equals(owner.getUuid()) || PlayerDataHandler.isFriends(owner, tile.getOwner())) {
                owned.put(tile.getGlobalPos(), tile);
            }
        }

        return owned;
    }
    public static boolean isSafeSpawnPoint(GlobalPos pos, MinecraftServer server) {
        HashMap<GlobalPos, OwnedTile> tiles = getServerState(server).tiles;
        BlockPos bPos = new BlockPos(pos.pos().getX(), 0, pos.pos().getZ());

        return !tiles.containsKey(pos) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.add(1, 0, 0))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.add(1, 0, 1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.add(0, 0, 1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.add(-1, 0, 1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.add(-1, 0, 0))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.add(-1, 0, -1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.add(0, 0, -1))) &&
                !tiles.containsKey(new GlobalPos(pos.dimension(), bPos.add(1, 0, -1)));
    }

    public static BlockPos getSafeSpawnPoint(ServerPlayerEntity p) {
        BlockPos spawn = null;

        for (int z = p.getChunkPos().z; z >= p.getChunkPos().z; z++) {
             spawn = SpawnLocating.findServerSpawnPoint(p.getServerWorld(), new ChunkPos(p.getChunkPos().x, z));

             // found safe spawnpoint
            if (isSafeSpawnPoint(new GlobalPos(p.getWorld().getRegistryKey(), spawn), p.getServer()) && p.teleport(spawn.getX(), spawn.getY(), spawn.getZ(), false)) {
                return spawn;
            }
        }

        return spawn;
    }

    public static OwnedTile unlockTile(ServerPlayerEntity player, int x, int z, RegistryKey<World> world) throws TileAlreadyUnlockedException {
        TileHandler handler = getServerState(player.getServer());
        GlobalPos pos = new GlobalPos(world, new BlockPos(x, 0, z));

        if (handler.tiles.get(pos) != null) {
            throw new TileAlreadyUnlockedException();
        }

        OwnedTile tile = new OwnedTile(pos, player);

        handler.tiles.put(pos, tile);
        handler.markDirty();

        player.getServer().getPlayerManager().getPlayerList().forEach((srvpl) -> {
            ServerPlayNetworking.send(srvpl, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(srvpl), getOwnedOrFriendlyTiles(srvpl).size()));
            ServerPlayNetworking.send(srvpl, new SendTilesS2C(tile));
        });

        return tile;
    }

    public static void unlockStartingSquare(ServerPlayerEntity p, BlockPos pos, RegistryKey<World> world) {
        // unlock starter tiles
        TileHandler.unlockTile(p, pos.getX(), pos.getZ(), p.getWorld().getRegistryKey());
        TileHandler.unlockTile(p,pos.getX() + 1, pos.getZ() - 1, p.getWorld().getRegistryKey());
        TileHandler.unlockTile(p,pos.getX() + 1, pos.getZ(), p.getWorld().getRegistryKey());
        TileHandler.unlockTile(p,pos.getX() + 1, pos.getZ() + 1, p.getWorld().getRegistryKey());
        TileHandler.unlockTile(p,pos.getX(), pos.getZ() - 1, p.getWorld().getRegistryKey());
        TileHandler.unlockTile(p,pos.getX(), pos.getZ() + 1, p.getWorld().getRegistryKey());
        TileHandler.unlockTile(p,pos.getX() - 1, pos.getZ() - 1, p.getWorld().getRegistryKey());
        TileHandler.unlockTile(p,pos.getX() - 1, pos.getZ(), p.getWorld().getRegistryKey());
        TileHandler.unlockTile(p,pos.getX() - 1, pos.getZ() + 1, p.getWorld().getRegistryKey());
    }

    public static void sendTiles(ServerPlayerEntity player) {
        TileHandler handler = getServerState(player.getServer());

        for (OwnedTile tile: handler.tiles.values()) {
            ServerPlayNetworking.send(player, new SendTilesS2C(tile));
        }
    }

    public static void sendTile(MinecraftServer server, OwnedTile tile) {
        server.getPlayerManager().getPlayerList().forEach((srvpl) -> {
            ServerPlayNetworking.send(srvpl, new SendTilesS2C(tile));
        });


    }


    public static void clearAndSendTiles(MinecraftServer server) {
        TileHandler handler = getServerState(server);

        server.getPlayerManager().getPlayerList().forEach((srvpl) -> {
            ServerPlayNetworking.send(srvpl, new ClearRenderedTilesS2C());
            for (OwnedTile tile: handler.tiles.values()) {
                ServerPlayNetworking.send(srvpl, new SendTilesS2C(tile));
            }
        });

    }



    private static TileHandler getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        TileHandler state = persistentStateManager.getOrCreate(persistantStateType, "tileman.tiles");

        state.markDirty();

        return state;
    }

    private static TileHandler createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        TileHandler handler = new TileHandler();

        NbtList tiles = tag.getList("tiles", NbtElement.COMPOUND_TYPE);

        tiles.forEach((nbtTile) -> {
            NbtCompound tile = (NbtCompound) nbtTile;

            Identifier identifier = Identifier.of(tile.getString("dimension"));

            RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, identifier);


            handler.tiles.put(
                    new GlobalPos(dimension, new BlockPos(tile.getInt("posX"), 0, tile.getInt("posZ"))),
                    new OwnedTile(tile.getInt("posX"), tile.getInt("posZ"), dimension, tile.getUuid("owner")));
        });

        handler.easyMode = tag.getBoolean("easyMode");
        return handler;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList tiles = new NbtList();
        this.tiles.values().forEach((tile) -> {
            NbtCompound tileNbt = new NbtCompound();

            tileNbt.putInt("posX", tile.getX());
            tileNbt.putInt("posZ", tile.getZ());
            tileNbt.putString("dimension", tile.getDimension().getValue().toString());

            tileNbt.putUuid("owner", tile.getOwner());

            tiles.add(tileNbt);
        });

        nbt.put("tiles", tiles);
        nbt.putBoolean("easyMode", this.easyMode);
        return nbt;
    }

    // if players should get one tile to unlock after a full minecraft day of not getting any tiles
    public static boolean isEasyMode(MinecraftServer server) {
        return getServerState(server).easyMode;
    }

    public static void toggleEasyMode(MinecraftServer server) {
        TileHandler handler = TileHandler.getServerState(server);

        handler.easyMode = !handler.easyMode;
        handler.markDirty();
    }
}
