package com.whosalbercik.tileman.server;

import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;


/**
 * Controls the player based on what tiles they're standing on
 */
public class MovementHandler {


    /**
     * Should run every tick. Unlocks new tiles or pushes the player back if they cannot unlock tile.
     *
     * @param p ticked player
     */
    public static void tickHandler(ServerPlayer p) {
        Tile standing = TileHandler.getTile(p.getServer(), p.getBlockX(), p.getBlockZ(), p.serverLevel().dimension());

        Vec3 deltaMovement = p.getDeltaMovement();

        Tile heading = TileHandler.getTile(p.server, (int) (p.getBlockX() + deltaMovement.x), (int) (p.getBlockZ() + deltaMovement.z), p.serverLevel().dimension());

        int availableTiles = PlayerDataHandler.getPlayerAvailableTiles(p);

        // if standing on owned tile
        if (standing instanceof OwnedTile owned) {
            // if tile is owned by player or friended
            if ((owned.getOwner().equals(p.getUUID()) || PlayerDataHandler.isFriends(p, owned.getOwner()))) {
                PlayerDataHandler.setLastSafeTile(p);
                return;
            } else {
                pushToSafeTile(p);
            }
        }
        // unlocking tiles
        if (availableTiles > 0 && !(heading instanceof OwnedTile) && PlayerDataHandler.isAutoClaimEnabled(p)) {
            PlayerDataHandler.removePlayerAvailableTiles(p, 1);
            TileHandler.unlockTile(p, p.getBlockX(), p.getBlockZ(), p.serverLevel().dimension());
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1f, 1f);
        } else  {
            pushToSafeTile(p);
        }

    }

    private static void pushToSafeTile(ServerPlayer p) {
        Vec3 standing = p.blockPosition().getCenter();
        GlobalPos safeTile = PlayerDataHandler.getLastSafeTile(p);
        Vec3 safePos = safeTile.pos().getCenter();

        double distance = Math.abs(standing.distanceTo(safePos));

        // if safe pos is in the same dimension as player and distance is close
        if (p.serverLevel().dimension().equals(safeTile.dimension()) && distance <= Math.sqrt(2)) {
            Vec3 offset = new Vec3(safePos.x() - standing.x(), p.getVehicle() == null ? safePos.y() - standing.y() : safePos.y() - standing.y() - 1, safePos.z() - standing.z());
            // if player is in some sort of vehicle, then move the vehicle, not the player
            if (p.getVehicle() != null) {
                p.getVehicle().push(offset.scale(0.1));
                return;
            }
            p.push(offset.scale(0.1));

            p.connection.send(new ClientboundSetEntityMotionPacket(p));
            } else { // if player in different dimension then teleport
            GlobalPos pos = PlayerDataHandler.getLastSafeTile(p);
            p.teleportTo(p.getServer().getLevel(pos.dimension()), safePos.x, safePos.y, safePos.z, p.getYRot(), p.getXRot());
        }
    }

}
