package com.whosalbercik.tileman.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.networking.SendFriendsS2C;
import com.whosalbercik.tileman.networking.SendSidePanelDataS2C;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import com.whosalbercik.tileman.server.PlayerInvites;
import com.whosalbercik.tileman.tile.TileHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FriendCommand {

    public static int invite(CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        GameProfile invited = source.getArgument("player", GameProfileArgumentType.GameProfileArgument.class).getNames(source.getSource()).stream().toList().getFirst();

        PlayerInvites.saveInvite(source.getSource().getPlayer().getUuid(), invited.getId());
        ModLogger.sendInfo(source.getSource().getPlayerOrThrow(), "You have successfully sent an invite!");

        ServerPlayerEntity invitedPlayer =  source.getSource().getServer().getPlayerManager().getPlayer(invited.getId());

        if (invitedPlayer.getUuid().equals(source.getSource().getPlayerOrThrow().getUuid())) {
            ModLogger.sendError(source.getSource().getPlayerOrThrow(), "You cannot invite yourself!");
            return 0;
        }

        if (invitedPlayer != null) ModLogger.sendInfo(invitedPlayer, "You have received an invite from " + source.getSource().getPlayerOrThrow().getName().getString());
        return 0;
    }

    public static int accept(CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        UUID inviterUuid = PlayerInvites.getInvite(source.getSource().getPlayerOrThrow().getUuid());

        if (inviterUuid == null) {
            ModLogger.sendError(source.getSource().getPlayerOrThrow(), "You have no invites!");
            return 0;
        }

        MinecraftServer server = source.getSource().getServer();

        ServerPlayerEntity inviter = server.getPlayerManager().getPlayer(inviterUuid);
        ServerPlayerEntity invited = source.getSource().getPlayerOrThrow();

        if (inviter == null) {
            ModLogger.sendError(source.getSource().getPlayerOrThrow(), "Inviter is not online!");
            return 0;
        }


        PlayerDataHandler.addPlayerFriends(invited, inviter);



        ModLogger.sendInfo(source.getSource().getPlayerOrThrow(), "Invite Accepted!");

        return 1;
    }

    public static int remove(CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        ServerPlayerEntity p =  EntityArgumentType.getPlayer(source, "player");
        ServerPlayerEntity author = source.getSource().getPlayerOrThrow();

        if (!PlayerDataHandler.isFriends(p, author.getUuid())) {
            ModLogger.sendError(author, "You are not friends with " + p.getName().getString());
            return 0;
        }

        PlayerDataHandler.removePlayerFriends(p, author);
        ModLogger.sendInfo(p, "You are no longer friends with " +  author.getName().getString());
        ModLogger.sendInfo(author, "You are no longer friends with " +  p.getName().getString());
        return 1;
    }

    public static int listFriends(CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        ServerPlayerEntity author = source.getSource().getPlayerOrThrow();

        ModLogger.sendInfo(author, PlayerDataHandler.getPlayerFriends(author).stream().map((uuid) -> source.getSource().getServer().getPlayerManager().getPlayer(uuid) != null ? source.getSource().getServer().getPlayerManager().getPlayer(uuid).getName().getString() : uuid.toString())
                .collect(Collectors.joining(", ")));
        return 0;
    }
}
