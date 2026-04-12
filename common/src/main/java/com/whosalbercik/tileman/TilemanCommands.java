package com.whosalbercik.tileman;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.whosalbercik.tileman.commands.AdminCommand;
import com.whosalbercik.tileman.commands.FriendCommand;
import com.whosalbercik.tileman.commands.TilesCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Commands in Tileman
 */
public class TilemanCommands {


    /**
     * Run in appropriate event to register commands
     *
     * @param dispatcher the dispatcher
     */
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("tileman")
                .then(literal("transfer")
                        .then(literal("claimed")
                                .then(argument("newOwner",  EntityArgument.player())
                                        .executes(TilesCommand::transferOwnership)))
                        .then(literal("available")
                                .then(argument("newOwner",  EntityArgument.player())
                                        .then(argument("amountOfTiles", IntegerArgumentType.integer(1))
                                                .executes(TilesCommand::transfer)))))
                .then(literal("admin")
                        .requires((source) -> source.hasPermission(2))
                        .then(literal("giveTiles")
                                .then(argument("player", EntityArgument.player())
                                        .then(argument("amount", IntegerArgumentType.integer())
                                                .executes(AdminCommand::giveTiles))))
                        .then(literal("easyMode")
                                .executes(AdminCommand::easyMode)))

                .then(literal("friends")
                        .executes(FriendCommand::listFriends)
                        .then(literal("invite")
                                .then(argument("player", EntityArgument.player())
                                        .executes(FriendCommand::invite)))
                        .then(literal("accept")
                                .executes(FriendCommand::accept))
                        .then(literal("remove")
                                .then(argument("player", EntityArgument.player())
                                        .executes(FriendCommand::remove))))
        );

    }
}
