package com.whosalbercik.tileman;


import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.whosalbercik.tileman.commands.FriendCommand;
import com.whosalbercik.tileman.commands.TilesCommand;
import com.whosalbercik.tileman.exception.TileAlreadyUnlockedException;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class Tileman implements ModInitializer {
    public static final Identifier sendTiles = Identifier.of("tileman", "send_tiles");
    public static final Identifier clearRenderedTiles = Identifier.of("tileman", "clear_rendered");
    public static final Identifier sendSidePanel = Identifier.of("tileman", "send_side_panel");
    public static final Identifier sendFriends = Identifier.of("tileman", "send_friends");
    public static final Identifier transferOwnership = Identifier.of("tileman", "transfer_ownership");



    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(SendTilesS2C.ID, SendTilesS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearRenderedTilesS2C.ID, ClearRenderedTilesS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(SendSidePanelDataS2C.ID, SendSidePanelDataS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(SendFriendsS2C.ID, SendFriendsS2C.CODEC);

        PayloadTypeRegistry.playC2S().register(TransferOwnershipC2S.ID, TransferOwnershipC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TransferOwnershipC2S.ID, TileHandler::transferOwnership);

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(literal("tileman")
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
                            .then(literal("tiles")
                                    .then(literal("transfer")
                                        .then(argument("player", GameProfileArgumentType.gameProfile())
                                            .then(argument("amountOfTiles", IntegerArgumentType.integer(1))
                                                .executes(TilesCommand::transfer)))))
                            .then(literal("help")
                                    .executes((source) ->
                                    {ModLogger.sendInfo(source.getSource().getPlayerOrThrow(), "/tileman friends invite/accept/remove\n/tileman tiles transfer <player> <amount>\n/tileman selectedTiles transferOwnership <player>"); return 0;})));
                });




        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, sender, server) -> {

            if (serverPlayNetworkHandler.getSide() == NetworkSide.CLIENTBOUND) return;

            ServerPlayerEntity p = serverPlayNetworkHandler.getPlayer();

            ServerPlayNetworking.send(p, new ClearRenderedTilesS2C()); // Clear tiles that client has saved


            if (TileHandler.getOwnedTiles(p).isEmpty()) {
                try {
                    TileHandler.unlockTile(p, p.getBlockX(), p.getBlockZ(), p.getWorld().getRegistryKey());
                    TileHandler.unlockTile(p, p.getBlockX() + 1, p.getBlockZ() - 1, p.getWorld().getRegistryKey());
                    TileHandler.unlockTile(p, p.getBlockX() + 1, p.getBlockZ(), p.getWorld().getRegistryKey());
                    TileHandler.unlockTile(p, p.getBlockX() + 1, p.getBlockZ() + 1, p.getWorld().getRegistryKey());
                    TileHandler.unlockTile(p, p.getBlockX(), p.getBlockZ() - 1, p.getWorld().getRegistryKey());
                    TileHandler.unlockTile(p, p.getBlockX(), p.getBlockZ() + 1, p.getWorld().getRegistryKey());
                    TileHandler.unlockTile(p, p.getBlockX() - 1, p.getBlockZ() - 1, p.getWorld().getRegistryKey());
                    TileHandler.unlockTile(p, p.getBlockX() - 1, p.getBlockZ(), p.getWorld().getRegistryKey());
                    TileHandler.unlockTile(p, p.getBlockX() - 1, p.getBlockZ() + 1, p.getWorld().getRegistryKey());

                    p.setSpawnPoint(p.getWorld().getRegistryKey(), p.getBlockPos(), 1f, true, false);


                } catch (TileAlreadyUnlockedException e) {
                    sender.disconnect(Text.of("Your spawn is a tile of someone else. Contact an admin to create a safe place to spawn"));
                }
            }

            TileHandler.sendTiles(p);
            ServerPlayNetworking.send(p, new SendSidePanelDataS2C(PlayerDataHandler.getPlayerAvailableTiles(p), TileHandler.getOwnedOrFriendedTiles(p).size()));

        });

        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                MovementHandler.tickHandler(player);
            }
        });

        // Give player an available tile when an entity has been killed
        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, source) -> {
            if (livingEntity instanceof PlayerEntity || !(source.getAttacker() instanceof ServerPlayerEntity attacker) || livingEntity.getWorld().isClient()) return;


            PlayerDataHandler.addPlayerAvailableTiles((ServerPlayerEntity) attacker, 1);

            attacker.playSound(SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value(), 1f, 1f);


        });

    }
}
