package com.whosalbercik.tileman.server;

import com.whosalbercik.tileman.LoaderServices;
import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.networking.packet.SendFriendsS2C;
import com.whosalbercik.tileman.networking.packet.SendSidePanelDataS2C;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * Saves and returns information related to the player
 */
public class PlayerDataHandler extends SavedData {

    private final HashMap<UUID, PlayerTileData> playerData = new HashMap<>();
    
    @Override
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        CompoundTag playersNbt = new CompoundTag();
        
        
        playerData.forEach((uuid, playerData) -> {
            CompoundTag playerNbt = new CompoundTag();
            playerNbt.putInt("tileman.availableTiles", playerData.availableTiles);

            ListTag friends = new ListTag();
            playerData.friends.forEach((friend) -> friends.add(StringTag.valueOf(friend.toString())));

            playerNbt.put("tileman.friends", friends);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("tileman.players", playersNbt);

        return nbt;
    }

    /**
     * Create from nbt player data handler.
     *
     * @param tag            the tag
     * @param registryLookup the registry lookup
     * @return the player data handler
     */
    public static PlayerDataHandler createFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        PlayerDataHandler state = new PlayerDataHandler();

        CompoundTag playersNbt = tag.getCompound("tileman.players");
        playersNbt.getAllKeys().forEach(userUUID -> {
            UUID uuid = UUID.fromString(userUUID);

            int availableTiles = playersNbt.getCompound(userUUID).getInt("tileman.availableTiles");

            ListTag nbtFriends = playersNbt.getCompound(userUUID).getList("tileman.friends", Tag.TAG_STRING);
            HashSet<UUID> friends = new HashSet<>();

            nbtFriends.forEach((friendUuid) -> friends.add(UUID.fromString(friendUuid.getAsString())));

            state.playerData.put(uuid, new PlayerTileData(availableTiles, friends, null, null));
        });

        return state;
    }

    private static PlayerDataHandler getServerState(MinecraftServer server) {
        if (server.getLevel(Level.OVERWORLD).isClientSide) return null;

        DimensionDataStorage dataStorage = server.getLevel(Level.OVERWORLD).getDataStorage();

        PlayerDataHandler state = dataStorage.computeIfAbsent(
                new Factory<PlayerDataHandler>(
                        PlayerDataHandler::new,
                        PlayerDataHandler::createFromNbt,
                        null
                ),
                "tileman");

        // idk why this?? but works
        state.setDirty();

        return state;
    }

    private static PlayerTileData getOrCreate(ServerPlayer p) {
        PlayerDataHandler serverState = getServerState(p.level().getServer());

        return serverState.playerData.computeIfAbsent(p.getUUID(),  (uuid) -> {
            return PlayerTileData.getEmpty(p);
        });
    }

    /**
     * Gets player available tiles.
     *
     * @param player the player
     * @return how many tiles player can unlock
     */
    public static int getPlayerAvailableTiles(ServerPlayer player) {
        return getOrCreate(player).availableTiles;
    }

    /**
     * Gets players friends
     *
     * @param player the player
     * @return players friends
     */
    public static HashSet<UUID> getPlayerFriends(ServerPlayer player) {
        return getOrCreate(player).friends;
    }

    /**
     * Makes the two players friends
     *
     * @param player1 the player 1
     * @param player2 the player 2
     */
    public static void addPlayerFriends(ServerPlayer player1, ServerPlayer player2) {
        PlayerDataHandler serverState = getServerState(player1.level().getServer());

        PlayerTileData p1Data = getOrCreate(player1);
        PlayerTileData p2Data = getOrCreate(player2);

        p1Data.friends.add(player2.getUUID());
        p2Data.friends.add(player1.getUUID());

        serverState.playerData.put(player1.getUUID(), p1Data);
        serverState.playerData.put(player2.getUUID(), p2Data);
        serverState.setDirty();

        // update client side info
        sendPlayersFriends(player1);
        sendPlayersFriends(player2);
    }

    /**
     * Sends clients friends to client
     *
     * @param p client
     */
    public static void sendPlayersFriends(ServerPlayer p) {
        PlayerTileData data = getOrCreate(p);

        LoaderServices.PLATFORM.sendS2C(p, new SendFriendsS2C(data.friends));
        LoaderServices.PLATFORM.sendS2C(p, new SendSidePanelDataS2C(getPlayerAvailableTiles(p), TileHandler.getOwnedOrFriendlyTiles(p).size()));

    }

    /**
     * Stops the two players from being friends
     *
     * @param player1 the player 1
     * @param player2 the player 2
     */
    public static void removePlayerFriends(ServerPlayer player1, ServerPlayer player2) {
        PlayerDataHandler serverState = getServerState(player1.level().getServer());

        PlayerTileData p1data = getOrCreate(player1);
        PlayerTileData p2data = getOrCreate(player2);

        p1data.friends.remove(player2.getUUID());
        p2data.friends.remove(player1.getUUID());

        serverState.setDirty();

        sendPlayersFriends(player1);
        sendPlayersFriends(player2);
    }


    /**
     * Check if the two players are friends
     *
     * @param player1 the player 1
     * @param player2 the player 2
     * @return if they are friends
     */
    public static boolean isFriends(ServerPlayer player1, UUID player2) {
        PlayerTileData pData = getOrCreate(player1);

        return pData.friends.contains(player2);
    }

    /**
     * Add available tiles to the player
     *
     * @param player the player
     * @param amount the amount
     */
    public static void addPlayerAvailableTiles(ServerPlayer player, int amount) {
        PlayerDataHandler serverState = getServerState(player.level().getServer());

        PlayerTileData pData = getOrCreate(player);

        pData.availableTiles += amount;

        serverState.playerData.put(player.getUUID(), pData);
        serverState.setDirty();

        LoaderServices.PLATFORM.sendS2C(player, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(player), TileHandler.getOwnedOrFriendlyTiles(player).size()));

        // save last time tile was unlocked for sake of easy mode
        PlayerDataHandler.setLastTimeGainedTile(player);
    }

    /**
     * Removes amount from players available tiles
     *
     * @param player the player
     * @param amount the amount
     */
    public static void removePlayerAvailableTiles(ServerPlayer player, int amount) {
        PlayerDataHandler serverState = getServerState(player.level().getServer());

        PlayerTileData pData = getOrCreate(player);
        pData.availableTiles -= amount;

        serverState.playerData.put(player.getUUID(), pData);
        serverState.setDirty();

        LoaderServices.PLATFORM.sendS2C(player, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(player), TileHandler.getOwnedOrFriendlyTiles(player).size()));
    }


    /**
     * Sets last safe tile.
     * This is the tile player will be pushed back once unable to unlock tile and stepped on it
     *
     * @param player the player
     */
    public static void setLastSafeTile(ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.level().getServer());

        PlayerTileData pData = getOrCreate(player);
        pData.lastSafeTile = player.getVehicle() == null ? player.blockPosition().asLong() : player.blockPosition().above().asLong();
        pData.lastSafeDimension = player.level().dimension();

        serverState.playerData.put(player.getUUID(), pData);
        serverState.setDirty();
    }

    /**
     * Gets last safe tile.
     *
     * @param player the player
     * @return the last safe tile position
     */
    public static GlobalPos getLastSafeTile(ServerPlayer player) {
        PlayerTileData pData = getOrCreate(player);
        return new GlobalPos(pData.lastSafeDimension, BlockPos.of(pData.lastSafeTile));

    }


    /**
     * Sets selected tiles.
     *
     * @param tiles  the selected tiles
     * @param player the player
     */
    public static void setSelectedTiles(HashSet<OwnedTile> tiles, ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.level().getServer());

        PlayerTileData pData = getOrCreate(player);
        pData.selectedTiles = tiles;

        serverState.setDirty();

    }


    /**
     *  Gets if tile claiming enabled
     *
     * @param player the player
     * @return tile claiming enabled
     */
    public static boolean isAutoClaimEnabled(ServerPlayer player) {
        PlayerTileData pData = getOrCreate(player);

        return pData.autoClaimEnabled;
    }

    /**
     * enables or disables tile claiming for player
     *
     * @param autoClaim enable or disable claiming
     * @param player the player
     */
    public static void setAutoClaim(boolean autoClaim, ServerPlayer player) {
        PlayerDataHandler serverState = getServerState(player.level().getServer());

        PlayerTileData pData = getOrCreate(player);

        pData.autoClaimEnabled = autoClaim;

        serverState.playerData.put(player.getUUID(), pData);

        ChatFormatting color = pData.autoClaimEnabled ? ChatFormatting.GREEN : ChatFormatting.RED;
        String enabledText = pData.autoClaimEnabled ? "enabled" : "disabled";

        ModLogger.sendError(player, String.format("Tile auto claim %s%s", color, enabledText));
    }

    /**
     * Sets last time tile was gained. Used in EasyMode.
     *
     * @param p the player
     */
    public static void setLastTimeGainedTile(ServerPlayer p) {
        PlayerDataHandler serverState = getServerState(p.getServer());

        PlayerTileData pData = getOrCreate(p);

        // set lsat time to now
        pData.lastTimeClaimed = System.currentTimeMillis() / 1000;

        serverState.playerData.put(p.getUUID(), pData);
    }

    /**
     * Gets if player should get their easymode tile
     *
     * @param p the player
     * @return should get tile or not
     */
    public static boolean shouldGetEasyModeTile(ServerPlayer p) {
        PlayerDataHandler serverState = getServerState(p.getServer());

        PlayerTileData pData = getOrCreate(p);

        return System.currentTimeMillis() / 1000 - pData.lastTimeClaimed > 1200;
    }

    /**
     * Gets selected tiles.
     *
     * @param player the player
     * @return the selected tiles
     */
    public static HashSet<OwnedTile> getSelectedTiles(ServerPlayer player) {
        PlayerTileData pData = getOrCreate(player);
        return pData.selectedTiles;
    }
}
