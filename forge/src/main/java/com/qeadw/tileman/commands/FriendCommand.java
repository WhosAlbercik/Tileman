package com.qeadw.tileman.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.qeadw.tileman.ModLogger;
import com.qeadw.tileman.server.PlayerDataHandler;
import com.qeadw.tileman.server.PlayerInvites;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class FriendCommand {

    public static int invite(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(source, "player");
        GameProfile invited = profiles.iterator().next();

        ServerPlayer sender = source.getSource().getPlayerOrException();
        PlayerInvites.saveInvite(sender.getUUID(), invited.getId());
        ModLogger.sendInfo(sender, "You have successfully sent an invite!");

        ServerPlayer invitedPlayer = source.getSource().getServer().getPlayerList().getPlayer(invited.getId());

        if (invitedPlayer != null && invitedPlayer.getUUID().equals(sender.getUUID())) {
            ModLogger.sendError(sender, "You cannot invite yourself!");
            return 0;
        }

        if (invitedPlayer != null) {
            ModLogger.sendInfo(invitedPlayer, "You have received an invite from " + sender.getName().getString());
        }

        return 0;
    }

    public static int accept(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer player = source.getSource().getPlayerOrException();
        UUID inviterUuid = PlayerInvites.getInvite(player.getUUID());

        if (inviterUuid == null) {
            ModLogger.sendError(player, "You have no invites!");
            return 0;
        }

        MinecraftServer server = source.getSource().getServer();
        ServerPlayer inviter = server.getPlayerList().getPlayer(inviterUuid);

        if (inviter == null) {
            ModLogger.sendError(player, "Inviter is not online!");
            return 0;
        }

        PlayerDataHandler.addPlayerFriends(player, inviter);
        ModLogger.sendInfo(player, "Invite Accepted!");
        return 1;
    }

    public static int remove(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(source, "player");
        ServerPlayer author = source.getSource().getPlayerOrException();

        if (!PlayerDataHandler.isFriends(target, author.getUUID())) {
            ModLogger.sendError(author, "You are not friends with " + target.getName().getString());
            return 0;
        }

        PlayerDataHandler.removePlayerFriends(target, author);
        ModLogger.sendInfo(target, "You are no longer friends with " + author.getName().getString());
        ModLogger.sendInfo(author, "You are no longer friends with " + target.getName().getString());
        return 1;
    }

    public static int listFriends(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer author = source.getSource().getPlayerOrException();
        String friendsList = PlayerDataHandler.getPlayerFriends(author)
            .stream()
            .map(uuid -> {
                ServerPlayer player = source.getSource().getServer().getPlayerList().getPlayer(uuid);
                return player != null ? player.getName().getString() : uuid.toString();
            })
            .collect(Collectors.joining(", "));

        ModLogger.sendInfo(author, friendsList.isEmpty() ? "You have no friends." : friendsList);
        return 0;
    }
}
