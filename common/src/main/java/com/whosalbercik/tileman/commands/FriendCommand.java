package com.whosalbercik.tileman.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import com.whosalbercik.tileman.server.PlayerInvites;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>Commands related to friend adding, removing and listing</p>
 * <p>A player who is friended to another may stand on their tiles, however they may not transfer ownership of their tiles</p>
 */
public class FriendCommand {

    /**
     * Sends friend invite from the sender of the command to the <b>player</b> argument
     *
     * @param source requires <b>player</b> argument of {@link EntityArgument} type
     */
    public static int invite(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        
        ServerPlayer invitedPlayer = EntityArgument.getPlayer(source, "player");

        if (PlayerDataHandler.isFriends(source.getSource().getPlayerOrException(), invitedPlayer.getUUID())){
            ModLogger.sendError(source.getSource().getPlayerOrException(), "You are already friends!");
            return 0;
        }

        if (invitedPlayer.getUUID().equals(source.getSource().getPlayerOrException().getUUID())) {
            ModLogger.sendError(source.getSource().getPlayerOrException(), "You cannot invite yourself!");
            return 0;
        }

        PlayerInvites.saveInvite(source.getSource().getPlayer().getUUID(), invitedPlayer.getUUID());
        ModLogger.sendInfo(source.getSource().getPlayerOrException(), "You have successfully sent an invite!");

        if (invitedPlayer != null) ModLogger.sendInfo(invitedPlayer, "You have received an invite from " + source.getSource().getPlayerOrException().getName().getString());
        return 1;
    }

    /**
     * Accepts the pending invite and make the players friends
     *
     * @param source command source
     */
    public static int accept(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        UUID inviterUuid = PlayerInvites.getInvite(source.getSource().getPlayerOrException().getUUID());

        if (inviterUuid == null) {
            ModLogger.sendError(source.getSource().getPlayerOrException(), "You have no invites!");
            return 0;
        }

        MinecraftServer server = source.getSource().getServer();

        ServerPlayer inviter = server.getPlayerList().getPlayer(inviterUuid);
        ServerPlayer invited = source.getSource().getPlayerOrException();

        if (inviter == null) {
            ModLogger.sendError(source.getSource().getPlayerOrException(), "Inviter is not online!");
            return 0;
        }

        PlayerDataHandler.addPlayerFriends(invited, inviter);
        ModLogger.sendInfo(source.getSource().getPlayerOrException(), "Invite Accepted!");

        ;
        return 1;
    }

    /**
     * Removes <b>player</b> from authors friends
     *
     * @param source the source must have a <b>player</b> argument of {@link EntityArgument} type
     */
    public static int remove(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer p =  EntityArgument.getPlayer(source, "player");
        ServerPlayer author = source.getSource().getPlayerOrException();

        if (!PlayerDataHandler.isFriends(p, author.getUUID())) {
            ModLogger.sendError(author, "You are not friends with " + p.getName().getString());
            return 0;
        }

        PlayerDataHandler.removePlayerFriends(p, author);
        ModLogger.sendInfo(p, "You are no longer friends with " +  author.getName().getString());
        ModLogger.sendInfo(author, "You are no longer friends with " +  p.getName().getString());
        return 1;
    }

    /**
     * List friends of command author
     *
     * @param source the source
     */
    public static int listFriends(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer author = source.getSource().getPlayerOrException();

        ModLogger.sendInfo(author, PlayerDataHandler.getPlayerFriends(author).stream().map((uuid) -> source.getSource().getServer().getPlayerList().getPlayer(uuid) != null
                        ? source.getSource().getServer().getPlayerList().getPlayer(uuid).getName().getString() : uuid.toString())
                .collect(Collectors.joining(", ")));
        return 0;
    }
}
