package com.whosalbercik.tileman.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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
}

