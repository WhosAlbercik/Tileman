package com.whosalbercik.tileman.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import com.whosalbercik.tileman.server.TileHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;

import javax.swing.text.html.parser.Entity;

/**
 * Commands that require permission level 2
 */
public class AdminCommand {
    /**
     * Gives a certain number of avaiblae tiles to the <b>player</b> argument
     *
     * @param ctx must have <b>player</b> argument of type {@link EntityArgument}
     */
    public static int giveTiles(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(ctx, "player");

        PlayerDataHandler.addPlayerAvailableTiles(p, ctx.getArgument("amount", Integer.class));
        return 1;
    }

    /**
     * Toggles easy mode
     *
     * @param ctx no arguments required
     */
    public static int easyMode(CommandContext<CommandSourceStack> ctx) {
        TileHandler.toggleEasyMode(ctx.getSource().getServer());
        ctx.getSource().sendSystemMessage(ModLogger.getInfo(String.format("EasyMode has now been %s", TileHandler.isEasyMode(ctx.getSource().getServer()) ? "enabled" : "disabled")));
        return 1;
    }
}
