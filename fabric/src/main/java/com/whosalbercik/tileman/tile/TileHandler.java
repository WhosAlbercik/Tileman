package com.whosalbercik.tileman.tile;

import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.exception.TileAlreadyUnlockedException;
import com.whosalbercik.tileman.networking.ClearRenderedTilesS2C;
import com.whosalbercik.tileman.networking.SendSidePanelDataS2C;
import com.whosalbercik.tileman.networking.SendTilesS2C;
import com.whosalbercik.tileman.networking.TransferOwnershipC2S;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.ArrayList;

public class TileHandler extends PersistentState {
    public ArrayList<OwnedTile> tiles = new ArrayList<>();


    public static Tile getTile(MinecraftServer server, int x, int z, RegistryKey<World> dimension) {
        TileHandler handler = getServerState(server);

        for (OwnedTile tile: handler.tiles) {
            if (tile.getX() == x && tile.getZ() == z && tile.getDimension().equals(dimension)) {
                return tile;
            }
        }
        return new Tile(x, z, dimension);
    }

    public static ArrayList<OwnedTile> getOwnedTiles(ServerPlayerEntity owner) {
        TileHandler handler = getServerState(owner.getServer());

        ArrayList<OwnedTile> owned = new ArrayList<>();

        for (OwnedTile tile: handler.tiles) {
            if (tile.getOwner().equals(owner.getUuid())) {
                owned.add(tile);
            }
        }

        return owned;
    }

    public static ArrayList<OwnedTile> getOwnedOrFriendedTiles(ServerPlayerEntity owner) {
        TileHandler handler = getServerState(owner.getServer());

        ArrayList<OwnedTile> owned = new ArrayList<>();

        for (OwnedTile tile: handler.tiles) {
            if (tile.getOwner().equals(owner.getUuid()) || PlayerDataHandler.isFriends(owner, tile.getOwner())) {
                owned.add(tile);
            }
        }

        return owned;
    }

    public static OwnedTile unlockTile(ServerPlayerEntity player, int x, int z, RegistryKey<World> world) throws TileAlreadyUnlockedException {
        TileHandler handler = getServerState(player.getServer());
        GlobalPos pos = new GlobalPos(world, new BlockPos(x, 0, z));

        for (OwnedTile tile: handler.tiles) {
            if (tile.equals(pos) && !tile.getOwner().equals(player.getUuid())) {
                throw new TileAlreadyUnlockedException();
            }
        }
        OwnedTile tile = new OwnedTile(pos, player);

        handler.tiles.add(tile);
        handler.markDirty();

        player.getServer().getPlayerManager().getPlayerList().forEach((srvpl) -> {
            ServerPlayNetworking.send(srvpl, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(srvpl), getOwnedTiles(srvpl).size()));
            ServerPlayNetworking.send(srvpl, new SendTilesS2C(tile));
        });

        return tile;
    }

    public static void sendTiles(ServerPlayerEntity player) {
        TileHandler handler = getServerState(player.getServer());

        for (OwnedTile tile: handler.tiles) {
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
            for (OwnedTile tile: handler.tiles) {
                ServerPlayNetworking.send(srvpl, new SendTilesS2C(tile));
            }
        });

    }

    private static Type<TileHandler> type = new Type<>(
            TileHandler::new,
            TileHandler::createFromNbt,
            null
    );

    private static TileHandler getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        TileHandler state = persistentStateManager.getOrCreate(type, "tileman.tiles");

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


            handler.tiles.add(new OwnedTile(tile.getInt("posX"), tile.getInt("posZ"), dimension, tile.getUuid("owner")));
        });
        return handler;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList tiles = new NbtList();
        this.tiles.forEach((tile) -> {
            NbtCompound tileNbt = new NbtCompound();

            tileNbt.putInt("posX", tile.getX());
            tileNbt.putInt("posZ", tile.getZ());
            tileNbt.putString("dimension", tile.getDimension().getValue().toString());

            tileNbt.putUuid("owner", tile.getOwner());

            tiles.add(tileNbt);
        });

        nbt.put("tiles", tiles);
        return nbt;
    }


    public static ArrayList<OwnedTile> getTilesInNether(MinecraftServer server) {
        TileHandler handler = getServerState(server);

        ArrayList<OwnedTile> netherTiles = new ArrayList<>();

        for (OwnedTile tile: handler.tiles) {
            if (tile.getDimension().equals(RegistryKey.ofRegistry(DimensionTypes.THE_NETHER_ID))) {
                netherTiles.add(tile);
            }
        }
        return netherTiles;
    }

    public static ArrayList<OwnedTile> getTilesInEnd(MinecraftServer server) {
        TileHandler handler = getServerState(server);

        ArrayList<OwnedTile> endTiles = new ArrayList<>();

        for (OwnedTile tile: handler.tiles) {
            if (tile.getDimension().equals(RegistryKey.ofRegistry(DimensionTypes.THE_END_ID))) {
                endTiles.add(tile);
            }
        }
        return endTiles;
    }

}
