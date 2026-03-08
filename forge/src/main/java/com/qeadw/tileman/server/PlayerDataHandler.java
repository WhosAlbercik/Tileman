package com.qeadw.tileman.server;

import com.qeadw.tileman.ModLogger;
import com.qeadw.tileman.network.NetworkHandler;
import com.qeadw.tileman.network.packets.SendFriendsS2CPacket;
import com.qeadw.tileman.network.packets.SendSidePanelDataS2CPacket;
import com.qeadw.tileman.tile.OwnedTile;
import com.qeadw.tileman.tile.PlayerTileData;
import com.qeadw.tileman.tile.TileHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PlayerDataHandler extends SavedData {
    public HashMap<UUID, PlayerTileData> playerData = new HashMap<>();
    private static final String DATA_NAME = "tileman.available_tiles";

    public PlayerDataHandler() {
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        CompoundTag playersNbt = new CompoundTag();

        playerData.forEach((uuid, data) -> {
            CompoundTag playerNbt = new CompoundTag();
            playerNbt.putInt("tileman.availableTiles", data.availableTiles);

            ListTag friends = new ListTag();
            data.friends.forEach(friend -> friends.add(StringTag.valueOf(friend.toString())));
            playerNbt.put("tileman.friends", friends);

            playersNbt.put(uuid.toString(), playerNbt);
        });

        nbt.put("tileman.players", playersNbt);
        return nbt;
    }

    public static PlayerDataHandler load(CompoundTag tag) {
        PlayerDataHandler state = new PlayerDataHandler();
        CompoundTag playersNbt = tag.getCompound("tileman.players");

        for (String userUUID : playersNbt.getAllKeys()) {
            UUID uuid = UUID.fromString(userUUID);
            int availableTiles = playersNbt.getCompound(userUUID).getInt("tileman.availableTiles");
            ListTag nbtFriends = playersNbt.getCompound(userUUID).getList("tileman.friends", Tag.TAG_STRING);

            ArrayList<UUID> friends = new ArrayList<>();
            for (int i = 0; i < nbtFriends.size(); i++) {
                friends.add(UUID.fromString(nbtFriends.getString(i)));
            }

            state.playerData.put(uuid, new PlayerTileData(availableTiles, friends, null, null));
        }

        return state;
    }

    private static PlayerDataHandler getServerState(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        DimensionDataStorage storage = overworld.getDataStorage();
        PlayerDataHandler state = storage.computeIfAbsent(PlayerDataHandler::load, PlayerDataHandler::new, DATA_NAME);
        state.setDirty();
        return state;
    }

    public static int getPlayerAvailableTiles(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        return serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()))
            .availableTiles;
    }

    public static ArrayList<OwnedTile> getPlayerSelectedTiles(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        return serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()))
            .selectedTiles;
    }

    public static ArrayList<UUID> getPlayerFriends(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        return serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()))
            .friends;
    }

    public static void addPlayerFriends(ServerPlayer player1, ServerPlayer player2) {
        PlayerDataHandler serverState = getServerState(player1.getServer());

        PlayerTileData p1Data = serverState.playerData
            .computeIfAbsent(player1.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player1.blockPosition().asLong(), player1.level().dimension()));
        PlayerTileData p2Data = serverState.playerData
            .computeIfAbsent(player2.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player2.blockPosition().asLong(), player2.level().dimension()));

        p1Data.friends.add(player2.getUUID());
        p2Data.friends.add(player1.getUUID());

        serverState.playerData.put(player1.getUUID(), p1Data);
        serverState.playerData.put(player2.getUUID(), p2Data);
        serverState.setDirty();

        String player1Friends = String.join(";", getPlayerFriends(player1).stream().map(String::valueOf).toArray(String[]::new));
        String player2Friends = String.join(";", getPlayerFriends(player2).stream().map(String::valueOf).toArray(String[]::new));

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player1), new SendFriendsS2CPacket(player1Friends));
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player2), new SendFriendsS2CPacket(player2Friends));
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player1),
            new SendSidePanelDataS2CPacket(getPlayerAvailableTiles(player1), TileHandler.getOwnedOrFriendedTiles(player1).size()));
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player2),
            new SendSidePanelDataS2CPacket(getPlayerAvailableTiles(player2), TileHandler.getOwnedOrFriendedTiles(player2).size()));
    }

    public static void removePlayerFriends(ServerPlayer player1, ServerPlayer player2) {
        PlayerDataHandler serverState = getServerState(player1.getServer());

        serverState.playerData
            .computeIfAbsent(player1.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player1.blockPosition().asLong(), player1.level().dimension()))
            .friends.remove(player2.getUUID());

        serverState.playerData
            .computeIfAbsent(player2.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player2.blockPosition().asLong(), player2.level().dimension()))
            .friends.remove(player1.getUUID());

        serverState.setDirty();

        String player1Friends = String.join(";", getPlayerFriends(player1).stream().map(String::valueOf).toArray(String[]::new));
        String player2Friends = String.join(";", getPlayerFriends(player2).stream().map(String::valueOf).toArray(String[]::new));

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player1), new SendFriendsS2CPacket(player1Friends));
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player2), new SendFriendsS2CPacket(player2Friends));
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player1),
            new SendSidePanelDataS2CPacket(getPlayerAvailableTiles(player1), TileHandler.getOwnedOrFriendedTiles(player1).size()));
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player2),
            new SendSidePanelDataS2CPacket(getPlayerAvailableTiles(player2), TileHandler.getOwnedOrFriendedTiles(player2).size()));
    }

    public static boolean isFriendsWithOne(ServerPlayer player1, ArrayList<UUID> players) {
        PlayerDataHandler serverState = getServerState(player1.getServer());
        AtomicBoolean isFriendsWithOne = new AtomicBoolean(false);

        players.forEach(player2 -> {
            boolean areFriends = serverState.playerData
                    .computeIfAbsent(player1.getUUID(),
                        uuid -> new PlayerTileData(0, new ArrayList<>(), player1.blockPosition().asLong(), player1.level().dimension()))
                    .friends.contains(player2)
                && serverState.playerData
                    .computeIfAbsent(player2, uuid -> new PlayerTileData(0, new ArrayList<>(), null, null))
                    .friends.contains(player1.getUUID());

            if (areFriends) {
                isFriendsWithOne.set(true);
            }
        });

        return isFriendsWithOne.get();
    }

    public static boolean isFriends(ServerPlayer player1, UUID player2) {
        PlayerDataHandler serverState = getServerState(player1.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player1.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player1.blockPosition().asLong(), player1.level().dimension()));
        return pData.friends.contains(player2);
    }

    public static void addPlayerAvailableTiles(ServerPlayer player, int amount) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        pData.availableTiles += amount;
        serverState.playerData.put(player.getUUID(), pData);
        serverState.setDirty();

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new SendSidePanelDataS2CPacket(getPlayerAvailableTiles(player), TileHandler.getOwnedOrFriendedTiles(player).size()));
    }

    public static void removePlayerAvailableTiles(ServerPlayer player, int amount) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        pData.availableTiles -= amount;
        serverState.playerData.put(player.getUUID(), pData);
        serverState.setDirty();

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new SendSidePanelDataS2CPacket(getPlayerAvailableTiles(player), TileHandler.getOwnedOrFriendedTiles(player).size()));
    }

    public static void setLastSafeTile(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        BlockPos pos = player.getVehicle() == null ? player.blockPosition() : player.blockPosition().above();
        pData.lastSafeTile = pos.asLong();
        pData.lastSafeDimension = player.level().dimension();
        serverState.playerData.put(player.getUUID(), pData);
        serverState.setDirty();
    }

    public static GlobalPos getLastSafeTile(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        return GlobalPos.of(pData.lastSafeDimension, BlockPos.of(pData.lastSafeTile));
    }

    public static void addSelectedTile(ServerPlayer player, OwnedTile tile) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        if (!pData.selectedTiles.contains(tile)) {
            pData.selectedTiles.add(tile);
            serverState.setDirty();
        }
    }

    public static void clearSelectedTiles(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        pData.selectedTiles.clear();
        serverState.setDirty();
    }

    public static ArrayList<OwnedTile> getSelectedTiles(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        return pData.selectedTiles;
    }

    public static boolean isAutoClaimEnabled(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        return pData.autoClaimEnabled;
    }

    public static void setAutoClaim(ServerPlayer player, boolean enabled) {
        PlayerDataHandler serverState = getServerState(player.getServer());
        PlayerTileData pData = serverState.playerData
            .computeIfAbsent(player.getUUID(),
                uuid -> new PlayerTileData(0, new ArrayList<>(), player.blockPosition().asLong(), player.level().dimension()));

        pData.autoClaimEnabled = enabled;
        serverState.playerData.put(player.getUUID(), pData);

        ChatFormatting color = pData.autoClaimEnabled ? ChatFormatting.GREEN : ChatFormatting.RED;
        String enabledText = pData.autoClaimEnabled ? "enabled" : "disabled";
        ModLogger.sendError(player, String.format("Tile auto claim %s%s", color, enabledText));
    }
}
