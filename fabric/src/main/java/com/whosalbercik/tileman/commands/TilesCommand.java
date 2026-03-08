package com.whosalbercik.tileman.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;
import com.whosalbercik.tileman.tile.TileHandler;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;

public class TilesCommand {
    public static int transfer(CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        ServerPlayerEntity sender = source.getSource().getPlayerOrThrow();
        ServerPlayerEntity receiver = source.getSource().getServer().getPlayerManager().getPlayer(source.getArgument("player", GameProfileArgumentType.GameProfileArgument.class).getNames(source.getSource()).stream().toList().getFirst().getId());
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

    public static int transferOwnership(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity author = ctx.getSource().getPlayerOrThrow();

        OwnedTile standing = (OwnedTile) TileHandler.getTile(ctx.getSource().getServer(), author.getBlockX(), author.getBlockZ(), author.getWorld().getRegistryKey());

        ArrayList<OwnedTile> selectedTiles = PlayerDataHandler.getSelectedTiles(author);

        if (selectedTiles.contains(standing)) {
            ModLogger.sendError(author, "You cannot change ownership of the tile you are standing on!");
            return 0;
        }

        if (selectedTiles.isEmpty()) {
            ModLogger.sendError(author, "No Tiles have been selected");
            return 0;
        }

        ServerPlayerEntity newOwner = ctx.getSource().getServer().getPlayerManager().getPlayer(ctx.getArgument("player", GameProfileArgumentType.GameProfileArgument.class).getNames(ctx.getSource()).stream().toList().getFirst().getId());

        if (author.getUuid().equals(newOwner.getUuid())) {
            ModLogger.sendError(author, "You cannot send tiles to yourself!");
            return 0;
        }

        for (OwnedTile tile : selectedTiles) {
            // Get the tile that is saved in TileHandler
            OwnedTile actualTile = (OwnedTile) TileHandler.getTile(ctx.getSource().getServer(), tile.getX(), tile.getZ(), tile.getDimension());
            actualTile.transferOwnership(newOwner.getUuid());
            TileHandler.sendTile(ctx.getSource().getServer(), actualTile);
        }

        ModLogger.sendInfo(author, String.format("Ownership has been transferred for %s tile(s)", selectedTiles.size()));
        ModLogger.sendInfo(newOwner, String.format("You have received ownership for %s tile(s)", selectedTiles.size()));



        return 1;
    }
}

