package com.whosalbercik.tileman.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class CommandUtils {

    public static ServerPlayerEntity getPlayerArg(String argName, CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        UUID playerId = ((GameProfile) source.getArgument(argName, GameProfileArgumentType.GameProfileArgument.class).getNames(source.getSource()).toArray()[0]).getId();

        return source.getSource().getServer().getPlayerManager().getPlayer(playerId);
    }
}
