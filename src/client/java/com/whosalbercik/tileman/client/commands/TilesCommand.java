package com.whosalbercik.tileman.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.whosalbercik.tileman.client.AreaHandler;
import com.whosalbercik.tileman.networking.TransferOwnershipC2S;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class TilesCommand {
    // make this a server side command
    public static int transfer(CommandContext<FabricClientCommandSource> source) {

        String playerName = StringArgumentType.getString(source, "player");

        for (OwnedTile tileToSend: AreaHandler.selectedArea) {
            ClientPlayNetworking.send(new TransferOwnershipC2S(tileToSend, playerName));
        }

        AreaHandler.selectedArea.clear();
        return 0;
    }
}
