package com.whosalbercik.tileman;


import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.whosalbercik.tileman.commands.CommandUtils;
import com.whosalbercik.tileman.commands.FriendCommand;
import com.whosalbercik.tileman.commands.TilesCommand;
import com.whosalbercik.tileman.networking.*;

import com.whosalbercik.tileman.server.MovementHandler;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import com.whosalbercik.tileman.tile.TileHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import static net.minecraft.server.command.CommandManager.*;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;


public class Tileman implements ModInitializer {
    public static final Identifier sendTiles = Identifier.of("tileman", "send_tiles");
    public static final Identifier clearRenderedTiles = Identifier.of("tileman", "clear_rendered");
    public static final Identifier sendSidePanel = Identifier.of("tileman", "send_side_panel");
    public static final Identifier sendFriends = Identifier.of("tileman", "send_friends");
    public static final Identifier transferOwnership = Identifier.of("tileman", "transfer_ownership");
    public static final Identifier sendSelectedTile = Identifier.of("tileman", "send_selected_tile");
    public static final Identifier clearSelectedTiles = Identifier.of("tileman", "clear_selected_tiles");
    public static final Identifier setAutoClaim = Identifier.of("tileman", "set_auto_claim");

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(SendTilesS2C.ID, SendTilesS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearRenderedTilesS2C.ID, ClearRenderedTilesS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(SendSidePanelDataS2C.ID, SendSidePanelDataS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(SendFriendsS2C.ID, SendFriendsS2C.CODEC);

        PayloadTypeRegistry.playC2S().register(SetTileAutoClaimC2S.ID, SetTileAutoClaimC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetTileAutoClaimC2S.ID, PlayerDataHandler::setAutoClaim);

        PayloadTypeRegistry.playC2S().register(SendSelectedTileC2S.ID, SendSelectedTileC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SendSelectedTileC2S.ID, PlayerDataHandler::addSelectedTile);

        PayloadTypeRegistry.playC2S().register(ClearSelectedTilesC2S.ID, ClearSelectedTilesC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ClearSelectedTilesC2S.ID, PlayerDataHandler::clearSelectedTiles);

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(literal("tileman")
                                    .executes((source) -> {
                                        ModLogger.sendInfo(source.getSource().getPlayerOrThrow(), "/tileman friends invite/accept/remove\n/tileman transfer claimed/available <player> (<amount>)\n");
                                        return 0;
                                    })
                                    .then(literal("transfer")
                                                    .then(literal("claimed")
                                                            .then(argument("newOwner", GameProfileArgumentType.gameProfile())
                                                                    .executes(TilesCommand::transferOwnership)))
                                                    .then(literal("available")
                                                            .then(argument("newOwner", GameProfileArgumentType.gameProfile())
                                                                    .then(argument("amountOfTiles", IntegerArgumentType.integer(1))
                                                                            .executes(TilesCommand::transfer)))))
                                    .then(literal("admin")
                                            .requires((source) -> source.hasPermissionLevel(2))
                                            .then(literal("giveTiles")
                                                    .then(argument("player", GameProfileArgumentType.gameProfile())
                                                            .then(argument("amount", IntegerArgumentType.integer())
                                                                    .executes((source) ->
                                                                    {
                                                                        PlayerDataHandler.addPlayerAvailableTiles(CommandUtils.getPlayerArg("player", source), source.getArgument("amount", Integer.class));
                                                                        return 0;
                                                                    }))))
                                            .then(literal("easyMode")
                                                    .executes(TilesCommand::easyMode)))
                                    .then(literal("friends")
                                        .executes(FriendCommand::listFriends)
                                        .then(literal("invite")
                                                .then(argument("player", GameProfileArgumentType.gameProfile())
                                                    .executes(FriendCommand::invite)))
                                        .then(literal("accept")
                                                .executes(FriendCommand::accept))
                                        .then(literal("remove")
                                                .then(argument("player", EntityArgumentType.player())
                                                        .executes(FriendCommand::remove))))


                            );
                });




        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, sender, server) -> {

            if (serverPlayNetworkHandler.getSide() == NetworkSide.CLIENTBOUND) return;

            ServerPlayerEntity p = serverPlayNetworkHandler.getPlayer();

            ServerPlayNetworking.send(p, new ClearRenderedTilesS2C()); // Clear tiles that client has saved

            // if player has joined for the first time
            if (TileHandler.getOwnedTiles(p).isEmpty()) {
                BlockPos spawn;

                // if can safely spawn normally
                if (TileHandler.isSafeSpawnPoint(new GlobalPos(p.getWorld().getRegistryKey(), p.getBlockPos()), p.getServer())) {
                    spawn = p.getBlockPos();
                } else {
                    // create safe spawn point artificially
                    spawn = TileHandler.getSafeSpawnPoint(p);
                }

                TileHandler.unlockStartingSquare(p, spawn, p.getServerWorld().getRegistryKey());
                // set spawn point
                p.setSpawnPoint(p.getWorld().getRegistryKey(), spawn, 1f, true, false);

            }

            TileHandler.sendTiles(p);
            ServerPlayNetworking.send(p, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(p), TileHandler.getOwnedOrFriendlyTiles(p).size()));

        });

        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                MovementHandler.tickHandler(player);

                // give one extra tile in easy mode
                if (TileHandler.isEasyMode(server) && PlayerDataHandler.shouldGetEasyModeTile(player)) {
                    PlayerDataHandler.addPlayerAvailableTiles(player, 1);
                    ModLogger.sendInfo(player, Formatting.GREEN + "You have not gained a tile for a full day, so you got one extra!");
                }
            }
        });

        // Give player an available tile when an entity has been killed
        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, source) -> {
            if (livingEntity instanceof PlayerEntity || !(source.getAttacker() instanceof ServerPlayerEntity attacker) || livingEntity.getWorld().isClient()) return;

            PlayerDataHandler.addPlayerAvailableTiles(attacker, 1);

            // for easy mode
            PlayerDataHandler.setLastTimeGainedTile(attacker);

            attacker.playSound(SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value(), 1f, 1f);
        });

    }
}
