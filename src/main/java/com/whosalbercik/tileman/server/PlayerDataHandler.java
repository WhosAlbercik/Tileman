package com.whosalbercik.tileman.server;
import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.networking.*;
import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.PlayerTileData;
import com.whosalbercik.tileman.tile.TileHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerDataHandler extends PersistentState {
    public HashMap<UUID, PlayerTileData> playerData = new HashMap<>();
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound playersNbt = new NbtCompound();

        playerData.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putInt("tileman.availableTiles", playerData.availableTiles);

            NbtList friends = new NbtList();
            playerData.friends.forEach((friend) -> friends.add(NbtString.of(friend.toString())));

            playerNbt.put("tileman.friends", friends);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("tileman.players", playersNbt);

        return nbt;
    }

    public static PlayerDataHandler createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        PlayerDataHandler state = new PlayerDataHandler();

        NbtCompound playersNbt = tag.getCompound("tileman.players");
        playersNbt.getKeys().forEach(userUUID -> {
            UUID uuid = UUID.fromString(userUUID);

            int availableTiles = playersNbt.getCompound(userUUID).getInt("tileman.availableTiles");

            NbtList nbtFriends = playersNbt.getCompound(userUUID).getList("tileman.friends", NbtElement.STRING_TYPE);
            ArrayList<UUID> friends = new ArrayList<>();

            nbtFriends.forEach((friendUuid) -> friends.add(UUID.fromString(friendUuid.asString())));

            state.playerData.put(uuid, new PlayerTileData(availableTiles, friends, null, null));
        });

        return state;
    }


    private static Type<PlayerDataHandler> type = new Type<>(
            PlayerDataHandler::new,
            PlayerDataHandler::createFromNbt,
            null
    );

    private static PlayerDataHandler getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        PlayerDataHandler state = persistentStateManager.getOrCreate(type, "tileman.available_tiles");

        state.markDirty();

        return state;
    }

    public static int getPlayerAvailableTiles(ServerPlayerEntity player) {
        PlayerDataHandler serverState = getServerState(player.getWorld().getServer());


        return serverState.playerData.computeIfAbsent(player.getUuid(), uuid -> new PlayerTileData(0, new ArrayList<>(), player.getBlockPos().asLong(), player.getWorld().getRegistryKey())).availableTiles;
    }

    public static ArrayList<OwnedTile> getPlayerSelectedTiles(ServerPlayerEntity player) {
        PlayerDataHandler serverState = getServerState(player.getWorld().getServer());


        return serverState.playerData.computeIfAbsent(player.getUuid(), uuid -> new PlayerTileData(0, new ArrayList<>(), player.getBlockPos().asLong(), player.getWorld().getRegistryKey())).selectedTiles;
    }


    public static ArrayList<UUID> getPlayerFriends(ServerPlayerEntity player) {
        PlayerDataHandler serverState = getServerState(player.getWorld().getServer());


        return serverState.playerData.computeIfAbsent(player.getUuid(), uuid -> new PlayerTileData(0, new ArrayList<>(), player.getBlockPos().asLong(), player.getWorld().getRegistryKey())).friends;
    }

    public static void addPlayerFriends(ServerPlayerEntity player1, ServerPlayerEntity player2) {
        PlayerDataHandler serverState = getServerState(player1.getWorld().getServer());

        PlayerTileData p1Data = serverState.playerData.computeIfAbsent(player1.getUuid(), uuid -> new PlayerTileData(0, new ArrayList<>(), player1.getBlockPos().asLong(), player1.getWorld().getRegistryKey()));
        PlayerTileData p2Data = serverState.playerData.computeIfAbsent(player2.getUuid(), uuid -> new PlayerTileData(0, new ArrayList<>(), player2.getBlockPos().asLong(), player2.getWorld().getRegistryKey()));

        p1Data.friends.add(player2.getUuid());
        p2Data.friends.add(player1.getUuid());

        serverState.playerData.put(player1.getUuid(), p1Data);
        serverState.playerData.put(player2.getUuid(), p2Data);
        serverState.markDirty();

        String player1Friends = String.join(";", PlayerDataHandler.getPlayerFriends(player1).stream()
                .map(String::valueOf)
                .toArray(String[]::new));

        String player2Friends = String.join(";", PlayerDataHandler.getPlayerFriends(player2).stream()
                .map(String::valueOf)
                .toArray(String[]::new));


        ServerPlayNetworking.send(player1, new SendFriendsS2C(player1Friends));
        ServerPlayNetworking.send(player2, new SendFriendsS2C(player2Friends));

        ServerPlayNetworking.send(player1, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(player1), TileHandler.getOwnedOrFriendedTiles(player1).size()));
        ServerPlayNetworking.send(player2, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(player2), TileHandler.getOwnedOrFriendedTiles(player2).size()));


    }

    public static void removePlayerFriends(ServerPlayerEntity player1, ServerPlayerEntity player2) {
        PlayerDataHandler serverState = getServerState(player1.getWorld().getServer());

        serverState.playerData.computeIfAbsent(player1.getUuid(), uuid -> new PlayerTileData(0, new ArrayList<>(), player1.getBlockPos().asLong(), player1.getWorld().getRegistryKey())).friends.remove(player2.getUuid());
        serverState.playerData.computeIfAbsent(player2.getUuid(), uuid -> new PlayerTileData(0, new ArrayList<>(), player2.getBlockPos().asLong(), player2.getWorld().getRegistryKey())).friends.remove(player1.getUuid());
        serverState.markDirty();

        String player1Friends = String.join(";", PlayerDataHandler.getPlayerFriends(player1).stream()
                .map(String::valueOf)
                .toArray(String[]::new));

        String player2Friends = String.join(";", PlayerDataHandler.getPlayerFriends(player2).stream()
                .map(String::valueOf)
                .toArray(String[]::new));


        ServerPlayNetworking.send(player1, new SendFriendsS2C(player1Friends));
        ServerPlayNetworking.send(player2, new SendFriendsS2C(player2Friends));

        ServerPlayNetworking.send(player1, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(player1), TileHandler.getOwnedOrFriendedTiles(player1).size()));
        ServerPlayNetworking.send(player2, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(player2), TileHandler.getOwnedOrFriendedTiles(player2).size()));


    }


    public static boolean isFriendsWithOne(ServerPlayerEntity player1, ArrayList<UUID> players) {
        PlayerDataHandler serverState = getServerState(player1.getWorld().getServer());

        AtomicBoolean isFriendsWithOne = new AtomicBoolean(false);
        players.forEach((player2 -> {
            boolean areFriends = serverState.playerData.computeIfAbsent(player1.getUuid(), uuid -> new PlayerTileData(0, new ArrayList<>(), player1.getBlockPos().asLong(), player1.getWorld().getRegistryKey())).friends.contains(player2) && serverState.playerData.computeIfAbsent(player2, uuid -> new PlayerTileData(0, new ArrayList<>(), null, null)).friends.contains(player1.getUuid());

            if (areFriends) isFriendsWithOne.set(true);
        }));

        return isFriendsWithOne.get();
    }


    public static boolean isFriends(ServerPlayerEntity player1, UUID player2) {
        PlayerDataHandler serverState = getServerState(player1.getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(player1.getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), player1.getBlockPos().asLong(), player1.getWorld().getRegistryKey()));

        return pData.friends.contains(player2);
    }

    public static void addPlayerAvailableTiles(ServerPlayerEntity player, int amount) {
        PlayerDataHandler serverState = getServerState(player.getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(player.getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), player.getBlockPos().asLong(), player.getWorld().getRegistryKey()));

        pData.availableTiles += amount;

        serverState.playerData.put(player.getUuid(), pData);
        serverState.markDirty();

        ServerPlayNetworking.send(player, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(player), TileHandler.getOwnedOrFriendedTiles(player).size()));

    }

    public static void removePlayerAvailableTiles(ServerPlayerEntity player, int amount) {
        PlayerDataHandler serverState = getServerState(player.getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(player.getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), player.getBlockPos().asLong(), player.getWorld().getRegistryKey()));
        pData.availableTiles -= amount;

        serverState.playerData.put(player.getUuid(), pData);
        serverState.markDirty();

        ServerPlayNetworking.send(player, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(player), TileHandler.getOwnedOrFriendedTiles(player).size()));
    }


    public static void setLastSafeTile(ServerPlayerEntity player) {
        PlayerDataHandler serverState = getServerState(player.getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(player.getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), player.getBlockPos().asLong(), player.getWorld().getRegistryKey()));
        pData.lastSafeTile = player.getVehicle() == null ? player.getBlockPos().asLong() : player.getBlockPos().up().asLong();
        pData.lastSafeDimension = player.getWorld().getRegistryKey();

        serverState.playerData.put(player.getUuid(), pData);
        serverState.markDirty();
    }

    public static GlobalPos getLastSafeTile(ServerPlayerEntity player) {
        PlayerDataHandler serverState = getServerState(player.getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(player.getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), player.getBlockPos().asLong(), player.getWorld().getRegistryKey()));
        return new GlobalPos(pData.lastSafeDimension, BlockPos.fromLong(pData.lastSafeTile));

    }

    public static void addSelectedTile(SendSelectedTileC2S packet, ServerPlayNetworking.Context ctx) {
        PlayerDataHandler serverState = getServerState(ctx.player().getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(ctx.player().getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), ctx.player().getBlockPos().asLong(), ctx.player().getWorld().getRegistryKey()));

        if (pData.selectedTiles.contains(packet.tile())) return;

        pData.selectedTiles.add(packet.tile());
        serverState.markDirty();

    }

    public static void clearSelectedTiles(ClearSelectedTilesC2S packet, ServerPlayNetworking.Context ctx) {
        PlayerDataHandler serverState = getServerState(ctx.player().getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(ctx.player().getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), ctx.player().getBlockPos().asLong(), ctx.player().getWorld().getRegistryKey()));

        pData.selectedTiles.clear();
        serverState.markDirty();
    }

    public static ArrayList<OwnedTile> getSelectedTiles(ServerPlayerEntity p) {
        PlayerDataHandler serverState = getServerState(p.getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(p.getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), p.getBlockPos().asLong(), p.getWorld().getRegistryKey()));

        return pData.selectedTiles;
    }

    public static boolean isAutoClaimEnabled(ServerPlayerEntity player) {
        PlayerDataHandler serverState = getServerState(player.getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(player.getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), player.getBlockPos().asLong(), player.getWorld().getRegistryKey()));

        return pData.autoClaimEnabled;
    }

    public static void setAutoClaim(SetTileAutoClaimC2S packet, ServerPlayNetworking.Context ctx) {
        PlayerDataHandler serverState = getServerState(ctx.player().getWorld().getServer());

        PlayerTileData pData = serverState.playerData.computeIfAbsent(ctx.player().getUuid(), (uuid) -> new PlayerTileData(0, new ArrayList<>(), ctx.player().getBlockPos().asLong(), ctx.player().getWorld().getRegistryKey()));


        pData.autoClaimEnabled = packet.autoClaimEnabled();

        serverState.playerData.put(ctx.player().getUuid(), pData);
        Formatting color = pData.autoClaimEnabled ? Formatting.GREEN : Formatting.RED;
        String enabledText = pData.autoClaimEnabled ? "enabled" : "disabled";

        ModLogger.sendError(ctx.player(), String.format("Tile auto claim %s%s", color, enabledText));
    }
}
