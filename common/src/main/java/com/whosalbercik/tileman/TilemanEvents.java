package com.whosalbercik.tileman;

import com.whosalbercik.tileman.server.MovementHandler;
import com.whosalbercik.tileman.server.PlayerDataHandler;
import com.whosalbercik.tileman.server.TileHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Events that should run server-side
 */
public class TilemanEvents {

    /**
     * Runs when player joins server
     *
     * @param p the player
     */
    public static void serverSideJoin(ServerPlayer p) {
        // if player has joined for the first time
        if (TileHandler.getOwnedTiles(p).isEmpty()) {
            BlockPos spawn;

            // if can safely spawn normally
            if (TileHandler.isSafeSpawnPoint(new GlobalPos(p.level().dimension(), p.blockPosition()), p.getServer())) {
                spawn = p.blockPosition();
            } else {
                // create safe spawn point artificially
                spawn = TileHandler.getSafeSpawnPoint(p);
                p.teleport(new TeleportTransition(p.serverLevel(), Vec3.atCenterOf(spawn), Vec3.ZERO, 0f, 0f, TeleportTransition.DO_NOTHING));
            }

            TileHandler.unlockStartingSquare(p, spawn, p.serverLevel().dimension());
            // set spawn point
            p.setRespawnPosition(p.level().dimension(), spawn, 1f, true, false);

        }
        // reset timer
        PlayerDataHandler.setLastTimeGainedTile(p);

        // send client side info
        PlayerDataHandler.sendPlayersFriends(p);
        TileHandler.sendTiles(p);
    }


    /**
     * Ticks player server-side
     *
     * @param player the player
     */
    public static void tickPlayer(ServerPlayer player) {
        MovementHandler.tickHandler(player);

        // give one extra tile in easy mode
        if (TileHandler.isEasyMode(player.server) && PlayerDataHandler.shouldGetEasyModeTile(player)) {
            PlayerDataHandler.addPlayerAvailableTiles(player, 1);
            ModLogger.sendInfo(player, ChatFormatting.GREEN + "You have not gained a tile for a full day, so you got one extra!");
        }
    }


    /**
     * Runs when a {@link LivingEntity} dies
     *
     * @param dead   the dead {@link LivingEntity}
     * @param source the damage source
     */
    public static void livingEntityDead(LivingEntity dead, DamageSource source) {
        if (dead instanceof Player || !(source.getEntity() instanceof ServerPlayer attacker) || dead.level().isClientSide) return;

        PlayerDataHandler.addPlayerAvailableTiles(attacker, 1);

        // for easy mode
        PlayerDataHandler.setLastTimeGainedTile(attacker);

        attacker.playSound(SoundEvents.NOTE_BLOCK_COW_BELL.value(), 1f, 1f);
    }
}
