package com.whosalbercik.tileman.commands;

import java.util.HashSet;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import com.whosalbercik.tileman.server.TileHandler;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

/**
 * Commands related to transfering available and claimed tiles
 */
public class TilesCommand {
    /**
     * Transfers available <b>amountOfTiles</b> tiles from command author to <b>newOwner</b>
     *
     * @param ctx context, must have <b>player</b> argument of type {@link EntityArgument} and <b>amountOftiles</b> argument of type {@link Integer}
     */
    public static int transfer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();

        ServerPlayer receiver = EntityArgument.getPlayer(ctx, "newOwner");

        int amount = ctx.getArgument("amountOfTiles", Integer.class);

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

    /**
     * Transfers ownership of selected, owned tiles from command author to <b>newOwner</b>
     *
     * @param ctx context, must have <b>newOwner</b> argument of type {@link EntityArgument}
     */
    public static int transferOwnership(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer author = ctx.getSource().getPlayerOrException();

        // has to be owned because is standing on
        OwnedTile standing = (OwnedTile) TileHandler.getTile(ctx.getSource().getServer(), author.getBlockX(), author.getBlockZ(), author.level().dimension());

        HashSet<OwnedTile> selectedTiles = PlayerDataHandler.getSelectedTiles(author);

        if (selectedTiles.contains(standing)) {
            ModLogger.sendError(author, "You cannot change ownership of the tile you are standing on!");
            return 0;
        }

        if (selectedTiles.isEmpty()) {
            ModLogger.sendError(author, "No Tiles have been selected");
            return 0;
        }

        ServerPlayer newOwner = EntityArgument.getPlayer(ctx, "newOwner");

        if (author.getUUID().equals(newOwner.getUUID())) {
            ModLogger.sendError(author, "You cannot send tiles to yourself!");
            return 0;
        }

        for (OwnedTile tile : selectedTiles) {
            // Get the tile that is saved in TileHandler
            OwnedTile actualTile = (OwnedTile) TileHandler.getTile(ctx.getSource().getServer(), tile.getX(), tile.getZ(), tile.getDimension());
            actualTile.transferOwnership(newOwner.getUUID());
            TileHandler.sendTile(ctx.getSource().getServer(), actualTile);
        }

        ModLogger.sendInfo(author, String.format("Ownership has been transferred for %s tile(s)", selectedTiles.size()));
        ModLogger.sendInfo(newOwner, String.format("You have received ownership for %s tile(s)", selectedTiles.size()));

        return 0;
    }
}

