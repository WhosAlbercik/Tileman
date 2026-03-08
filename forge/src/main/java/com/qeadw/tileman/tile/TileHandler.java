package com.qeadw.tileman.tile;

import com.qeadw.tileman.exception.TileAlreadyUnlockedException;
import com.qeadw.tileman.network.NetworkHandler;
import com.qeadw.tileman.network.packets.ClearRenderedTilesS2CPacket;
import com.qeadw.tileman.network.packets.SendSidePanelDataS2CPacket;
import com.qeadw.tileman.network.packets.SendTilesS2CPacket;
import com.qeadw.tileman.server.PlayerDataHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.UUID;

public class TileHandler extends SavedData {
    public ArrayList<OwnedTile> tiles = new ArrayList<>();
    private static final String DATA_NAME = "tileman.tiles";

    public TileHandler() {
    }

    public static Tile getTile(MinecraftServer server, int x, int z, ResourceKey<Level> dimension) {
        TileHandler handler = getServerState(server);

        for (OwnedTile tile : handler.tiles) {
            if (tile.getX() == x && tile.getZ() == z && tile.getDimension().equals(dimension)) {
                return tile;
            }
        }

        return new Tile(x, z, dimension);
    }

    public static ArrayList<OwnedTile> getOwnedTiles(ServerPlayer owner) {
        TileHandler handler = getServerState(owner.getServer());
        ArrayList<OwnedTile> owned = new ArrayList<>();

        for (OwnedTile tile : handler.tiles) {
            if (tile.getOwner().equals(owner.getUUID())) {
                owned.add(tile);
            }
        }

        return owned;
    }

    public static ArrayList<OwnedTile> getOwnedOrFriendedTiles(ServerPlayer owner) {
        TileHandler handler = getServerState(owner.getServer());
        ArrayList<OwnedTile> owned = new ArrayList<>();

        for (OwnedTile tile : handler.tiles) {
            if (tile.getOwner().equals(owner.getUUID()) || PlayerDataHandler.isFriends(owner, tile.getOwner())) {
                owned.add(tile);
            }
        }

        return owned;
    }

    public static OwnedTile unlockTile(ServerPlayer player, int x, int z, ResourceKey<Level> world) throws TileAlreadyUnlockedException {
        TileHandler handler = getServerState(player.getServer());
        GlobalPos pos = GlobalPos.of(world, new BlockPos(x, 0, z));

        for (OwnedTile tile : handler.tiles) {
            if (tile.equals(pos) && !tile.getOwner().equals(player.getUUID())) {
                throw new TileAlreadyUnlockedException();
            }
        }

        OwnedTile newTile = new OwnedTile(pos, player);
        handler.tiles.add(newTile);
        handler.setDirty();

        // Notify all players
        for (ServerPlayer srvpl : player.getServer().getPlayerList().getPlayers()) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> srvpl),
                new SendSidePanelDataS2CPacket(PlayerDataHandler.getPlayerAvailableTiles(srvpl), getOwnedTiles(srvpl).size()));
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> srvpl),
                new SendTilesS2CPacket(newTile));
        }

        return newTile;
    }

    public static void sendTiles(ServerPlayer player) {
        TileHandler handler = getServerState(player.getServer());

        for (OwnedTile tile : handler.tiles) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SendTilesS2CPacket(tile));
        }
    }

    public static void sendTile(MinecraftServer server, OwnedTile tile) {
        for (ServerPlayer srvpl : server.getPlayerList().getPlayers()) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> srvpl),
                new SendTilesS2CPacket(tile));
        }
    }

    public static void clearAndSendTiles(MinecraftServer server) {
        TileHandler handler = getServerState(server);

        for (ServerPlayer srvpl : server.getPlayerList().getPlayers()) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> srvpl),
                new ClearRenderedTilesS2CPacket());

            for (OwnedTile tile : handler.tiles) {
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> srvpl),
                    new SendTilesS2CPacket(tile));
            }
        }
    }

    private static TileHandler getServerState(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        DimensionDataStorage storage = overworld.getDataStorage();
        TileHandler state = storage.computeIfAbsent(TileHandler::load, TileHandler::new, DATA_NAME);
        state.setDirty();
        return state;
    }

    public static TileHandler load(CompoundTag tag) {
        TileHandler handler = new TileHandler();
        ListTag tiles = tag.getList("tiles", Tag.TAG_COMPOUND);

        for (int i = 0; i < tiles.size(); i++) {
            CompoundTag tileTag = tiles.getCompound(i);
            ResourceLocation identifier = new ResourceLocation(tileTag.getString("dimension"));
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, identifier);
            handler.tiles.add(new OwnedTile(
                tileTag.getInt("posX"),
                tileTag.getInt("posZ"),
                dimension,
                tileTag.getUUID("owner")
            ));
        }

        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag tilesTag = new ListTag();

        for (OwnedTile tile : this.tiles) {
            CompoundTag tileNbt = new CompoundTag();
            tileNbt.putInt("posX", tile.getX());
            tileNbt.putInt("posZ", tile.getZ());
            tileNbt.putString("dimension", tile.getDimension().location().toString());
            tileNbt.putUUID("owner", tile.getOwner());
            tilesTag.add(tileNbt);
        }

        nbt.put("tiles", tilesTag);
        return nbt;
    }

    public static ArrayList<OwnedTile> getTilesInNether(MinecraftServer server) {
        TileHandler handler = getServerState(server);
        ArrayList<OwnedTile> netherTiles = new ArrayList<>();

        for (OwnedTile tile : handler.tiles) {
            if (tile.getDimension().equals(Level.NETHER)) {
                netherTiles.add(tile);
            }
        }

        return netherTiles;
    }

    public static ArrayList<OwnedTile> getTilesInEnd(MinecraftServer server) {
        TileHandler handler = getServerState(server);
        ArrayList<OwnedTile> endTiles = new ArrayList<>();

        for (OwnedTile tile : handler.tiles) {
            if (tile.getDimension().equals(Level.END)) {
                endTiles.add(tile);
            }
        }

        return endTiles;
    }
}
