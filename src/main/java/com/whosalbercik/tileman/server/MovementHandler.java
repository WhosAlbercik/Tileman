package com.whosalbercik.tileman.server;

import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;
import com.whosalbercik.tileman.tile.TileHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;


public class MovementHandler {


    public static void tickHandler(ServerPlayerEntity p) {
        Tile standing = TileHandler.getTile(p.getServer(), p.getBlockX(), p.getBlockZ(), p.getWorld().getRegistryKey());

        int availableTiles = PlayerDataHandler.getPlayerAvailableTiles(p);

        // if standing on owned tile
        if (standing instanceof OwnedTile owned) {
            // if tile is owned by player or friended
            if ((owned.getOwner().equals(p.getUuid()) || PlayerDataHandler.isFriends(p, owned.getOwner()))) {
                PlayerDataHandler.setLastSafeTile(p);
                return;
            } else {
                pushToSafeTile(p);
            }
        }
        // unlocking tiles
        if (availableTiles > 0 && !(standing instanceof OwnedTile) && PlayerDataHandler.isAutoClaimEnabled(p)) {
            PlayerDataHandler.removePlayerAvailableTiles(p, 1);
            TileHandler.unlockTile(p, p.getBlockX(), p.getBlockZ(), p.getWorld().getRegistryKey());
            p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1f, 1f);
        } else  {
            pushToSafeTile(p);
        }

    }

    private static void pushToSafeTile(ServerPlayerEntity p) {
        Vec3d standing = p.getBlockPos().toCenterPos();
        GlobalPos safeTile = PlayerDataHandler.getLastSafeTile(p);
        Vec3d safePos = safeTile.pos().toCenterPos();

        double distance = Math.abs(standing.distanceTo(safePos));

        // if safe pos is in the same dimension as player and distance is close
        if (p.getWorld().getRegistryKey().equals(safeTile.dimension()) && distance <= 1.5f) {
            Vec3d offset = new Vec3d(safePos.getX() - standing.getX(), p.getVehicle() == null ? safePos.getY() - standing.getY() : safePos.getY() - standing.getY() - 1, safePos.getZ() - standing.getZ());
            // if player is in some sort of vehicle, then move the vehicle, not the player
            if (p.getVehicle() != null) {
                p.getVehicle().setVelocity(offset.multiply(0.1));
                p.getVehicle().velocityModified = true;
                return;
            }
            p.setVelocity(offset.multiply(0.1));
            p.velocityModified = true;
        } else { // if player in different dimension then teleport
            GlobalPos pos = PlayerDataHandler.getLastSafeTile(p);
            p.teleportTo(new TeleportTarget(p.getServer().getWorld(pos.dimension()), safePos, Vec3d.ZERO, p.getYaw(), p.getPitch(), entity -> {}));
        }
    }

}
