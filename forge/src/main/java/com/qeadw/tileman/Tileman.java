package com.qeadw.tileman;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.qeadw.tileman.commands.FriendCommand;
import com.qeadw.tileman.commands.TilesCommand;
import com.qeadw.tileman.exception.TileAlreadyUnlockedException;
import com.qeadw.tileman.network.NetworkHandler;
import com.qeadw.tileman.network.packets.ClearRenderedTilesS2CPacket;
import com.qeadw.tileman.network.packets.SendSidePanelDataS2CPacket;
import com.qeadw.tileman.server.MovementHandler;
import com.qeadw.tileman.server.PlayerDataHandler;
import com.qeadw.tileman.tile.TileHandler;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

@Mod(Tileman.MODID)
public class Tileman {
    public static final String MODID = "tileman";

    public Tileman() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("tileman")
                .executes(source -> {
                    ModLogger.sendInfo(
                        source.getSource().getPlayerOrException(),
                        "/tileman friends invite/accept/remove\n/tileman tiles transfer <player> <amount>\n/tileman selectedTiles transferOwnership <player>"
                    );
                    return 0;
                })
                .then(Commands.literal("transferOwnership")
                    .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(TilesCommand::transferOwnership)))
                .then(Commands.literal("friends")
                    .executes(FriendCommand::listFriends)
                    .then(Commands.literal("invite")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                            .executes(FriendCommand::invite)))
                    .then(Commands.literal("accept")
                        .executes(FriendCommand::accept))
                    .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(FriendCommand::remove))))
                .then(Commands.literal("tiles")
                    .then(Commands.literal("transfer")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                            .then(Commands.argument("amountOfTiles", IntegerArgumentType.integer(1))
                                .executes(TilesCommand::transfer)))))
        );
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        System.out.println("[TILEMAN DEBUG] onPlayerJoin fired!");
        if (event.getEntity() instanceof ServerPlayer player) {
            System.out.println("[TILEMAN DEBUG] Player is ServerPlayer: " + player.getName().getString());
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClearRenderedTilesS2CPacket());

            if (TileHandler.getOwnedTiles(player).isEmpty()) {
                try {
                    int blockX = player.getBlockX();
                    int blockZ = player.getBlockZ();
                    // Unlock 3x3 starting area (individual blocks, not chunks)
                    TileHandler.unlockTile(player, blockX, blockZ, player.level().dimension());
                    TileHandler.unlockTile(player, blockX + 1, blockZ - 1, player.level().dimension());
                    TileHandler.unlockTile(player, blockX + 1, blockZ, player.level().dimension());
                    TileHandler.unlockTile(player, blockX + 1, blockZ + 1, player.level().dimension());
                    TileHandler.unlockTile(player, blockX, blockZ - 1, player.level().dimension());
                    TileHandler.unlockTile(player, blockX, blockZ + 1, player.level().dimension());
                    TileHandler.unlockTile(player, blockX - 1, blockZ - 1, player.level().dimension());
                    TileHandler.unlockTile(player, blockX - 1, blockZ, player.level().dimension());
                    TileHandler.unlockTile(player, blockX - 1, blockZ + 1, player.level().dimension());
                    player.setRespawnPosition(player.level().dimension(), player.blockPosition(), 1.0F, true, false);
                } catch (TileAlreadyUnlockedException e) {
                    player.connection.disconnect(Component.literal("Your spawn is a tile of someone else. Contact an admin to create a safe place to spawn"));
                }
            }

            TileHandler.sendTiles(player);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SendSidePanelDataS2CPacket(PlayerDataHandler.getPlayerAvailableTiles(player), TileHandler.getOwnedOrFriendedTiles(player).size()));
        }
    }

    private static int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.getServer() != null) {
            tickCounter++;
            if (tickCounter % 100 == 0) {
                System.out.println("[TILEMAN DEBUG] Server tick #" + tickCounter + ", players: " + event.getServer().getPlayerList().getPlayers().size());
            }
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                MovementHandler.tickHandler(player);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer) &&
            event.getSource().getEntity() instanceof ServerPlayer attacker &&
            !event.getEntity().level().isClientSide()) {
            PlayerDataHandler.addPlayerAvailableTiles(attacker, 1);
            attacker.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        }
    }
}
