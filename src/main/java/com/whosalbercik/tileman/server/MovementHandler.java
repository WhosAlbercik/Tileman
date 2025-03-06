package com.whosalbercik.tileman.server;

import com.whosalbercik.tileman.ModLogger;
import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;
import com.whosalbercik.tileman.tile.TileHandler;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;


public class MovementHandler {


    public static void tickHandler(ServerPlayerEntity p) {


        Tile standing = TileHandler.getTile(p.getServer(), p.getBlockX(), p.getBlockZ(), p.getWorld().getRegistryKey());

        int availableTiles = PlayerDataHandler.getPlayerAvailableTiles(p);


        if (standing instanceof OwnedTile owned) {
            if ((owned.getOwner().equals(p.getUuid()) || PlayerDataHandler.isFriends(p, owned.getOwner()))) {
                if (!p.getWorld().getBlockState(p.getBlockPos()).isOf(Blocks.NETHER_PORTAL) && !p.getWorld().getBlockState(BlockPos.ofFloored(p.raycast(2f, 1f, false).getPos())).isOf(Blocks.END_PORTAL)) {
                    PlayerDataHandler.setLastSafeTile(p);
                }
                return;
            } else {
                pushToSafeTile(p);
            }
        }

        // if player has tiles, has available tiles and not standing on one of them
        if (availableTiles > 0 && !(standing instanceof OwnedTile)) {
            PlayerDataHandler.removePlayerAvailableTiles(p, 1);
            TileHandler.unlockTile(p, p.getBlockX(), p.getBlockZ(), p.getWorld().getRegistryKey());
            p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1f, 1f);
                return;
        }

        if (!(standing instanceof OwnedTile)) {
            pushToSafeTile(p);
        }
        // if player has tiles, doesnt have available tiles and not standing on one of them

    }

    private static void pushToSafeTile(ServerPlayerEntity p) {
        BlockPos standing = p.getBlockPos();
        GlobalPos safeTile = PlayerDataHandler.getLastSafeTile(p);
        if (TileHandler.getOwnedOrFriendedTiles(p).contains(TileHandler.getTile(p.server ,safeTile.pos().getX(), safeTile.pos().getZ(), p.getWorld().getRegistryKey()))) {
            Vec3d offset = new Vec3d(safeTile.pos().getX() - standing.getX() , 0, safeTile.pos().getZ() - standing.getZ());
            p.setVelocity(offset.multiply(0.1));
            p.velocityModified = true;
            return;
        } else {
            GlobalPos pos = PlayerDataHandler.getLastSafeTile(p);
            p.teleportTo(new TeleportTarget(p.getServer().getWorld(pos.dimension()), new Vec3d(pos.pos()), Vec3d.ZERO, 1f, 1f, entity -> {}));
            ModLogger.sendWarning(p, "You nearly got stuck without a way to leave the portal but we saved you");
        }
    }

}
