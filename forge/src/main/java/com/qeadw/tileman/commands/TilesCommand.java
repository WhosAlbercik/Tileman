package com.qeadw.tileman.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.qeadw.tileman.ModLogger;
import com.qeadw.tileman.server.PlayerDataHandler;
import com.qeadw.tileman.tile.OwnedTile;
import com.qeadw.tileman.tile.TileHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;

public class TilesCommand {

    public static int transfer(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer sender = source.getSource().getPlayerOrException();
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(source, "player");
        GameProfile receiverProfile = profiles.iterator().next();

        ServerPlayer receiver = source.getSource().getServer().getPlayerList().getPlayer(receiverProfile.getId());
        int amount = source.getArgument("amountOfTiles", Integer.class);

        if (receiver == null) {
            ModLogger.sendError(sender, "Player could not be found!");
            return 0;
        }

        int sendersAmount = PlayerDataHandler.getPlayerAvailableTiles(sender);
        if (sendersAmount < amount) {
            ModLogger.sendError(sender, "You do not have enough available tiles!");
            return 0;
        }

        PlayerDataHandler.removePlayerAvailableTiles(sender, amount);
        PlayerDataHandler.addPlayerAvailableTiles(receiver, amount);
        ModLogger.sendInfo(sender, "You have successfully sent " + amount + " tiles to " + receiver.getName().getString());
        ModLogger.sendInfo(receiver, "You have received " + amount + " tiles from " + sender.getName().getString());
        return 0;
    }

    public static int transferOwnership(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer author = ctx.getSource().getPlayerOrException();

        int chunkX = author.getBlockX() >> 4;
        int chunkZ = author.getBlockZ() >> 4;

        OwnedTile standing = (OwnedTile) TileHandler.getTile(
            ctx.getSource().getServer(), chunkX, chunkZ, author.level().dimension()
        );

        ArrayList<OwnedTile> selectedTiles = PlayerDataHandler.getSelectedTiles(author);

        if (selectedTiles.contains(standing)) {
            ModLogger.sendError(author, "You cannot change ownership of the tile you are standing on!");
            return 0;
        }

        if (selectedTiles.isEmpty()) {
            ModLogger.sendError(author, "No Tiles have been selected");
            return 0;
        }

        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
        GameProfile newOwnerProfile = profiles.iterator().next();
        ServerPlayer newOwner = ctx.getSource().getServer().getPlayerList().getPlayer(newOwnerProfile.getId());

        if (newOwner == null) {
            ModLogger.sendError(author, "Player could not be found!");
            return 0;
        }

        if (author.getUUID().equals(newOwner.getUUID())) {
            ModLogger.sendError(author, "You cannot send tiles to yourself!");
            return 0;
        }

        for (OwnedTile tile : selectedTiles) {
            OwnedTile actualTile = (OwnedTile) TileHandler.getTile(
                ctx.getSource().getServer(), tile.getX(), tile.getZ(), tile.getDimension()
            );
            actualTile.transferOwnership(newOwner.getUUID());
            TileHandler.sendTile(ctx.getSource().getServer(), actualTile);
        }

        ModLogger.sendInfo(author, String.format("Ownership has been transferred for %s tile(s)", selectedTiles.size()));
        ModLogger.sendInfo(newOwner, String.format("You have received ownership for %s tile(s)", selectedTiles.size()));
        return 1;
    }
}
