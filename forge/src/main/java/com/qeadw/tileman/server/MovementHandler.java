package com.qeadw.tileman.server;

import com.qeadw.tileman.tile.OwnedTile;
import com.qeadw.tileman.tile.Tile;
import com.qeadw.tileman.tile.TileHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MovementHandler {

    private static int debugCounter = 0;

    public static void tickHandler(ServerPlayer p) {
        Tile standing = TileHandler.getTile(p.getServer(), p.getBlockX(), p.getBlockZ(), p.level().dimension());
        int availableTiles = PlayerDataHandler.getPlayerAvailableTiles(p);

        debugCounter++;
        if (debugCounter % 100 == 0) {
            System.out.println("[TILEMAN DEBUG] tickHandler: standing=" + (standing instanceof com.qeadw.tileman.tile.OwnedTile ? "OWNED" : "NOT_OWNED") +
                ", availableTiles=" + availableTiles + ", blockX=" + p.getBlockX() + ", blockZ=" + p.getBlockZ());
        }

        if (standing instanceof OwnedTile owned) {
            if (owned.getOwner().equals(p.getUUID()) || PlayerDataHandler.isFriends(p, owned.getOwner())) {
                if (!p.level().getBlockState(p.blockPosition()).is(Blocks.LAVA)
                    && !p.level().getBlockState(BlockPos.containing(p.pick(2.0, 1.0F, false).getLocation())).is(Blocks.WATER)) {
                    PlayerDataHandler.setLastSafeTile(p);
                }
                return;
            }

            pushToSafeTile(p);
        }

        if (availableTiles > 0 && !(standing instanceof OwnedTile) && PlayerDataHandler.isAutoClaimEnabled(p)) {
            PlayerDataHandler.removePlayerAvailableTiles(p, 1);
            TileHandler.unlockTile(p, p.getBlockX(), p.getBlockZ(), p.level().dimension());
            p.level().playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (!(standing instanceof OwnedTile)) {
            pushToSafeTile(p);
        }
    }

    private static void pushToSafeTile(ServerPlayer p) {
        BlockPos standing = p.blockPosition();
        GlobalPos safeTile = PlayerDataHandler.getLastSafeTile(p);

        Vec3 start = p.getVehicle() == null
            ? Vec3.atCenterOf(p.blockPosition())
            : Vec3.atCenterOf(p.blockPosition().above());
        Vec3 end = Vec3.atCenterOf(safeTile.pos());

        HitResult result = p.level().clip(new ClipContext(
            start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
            p.getVehicle() == null ? p : p.getVehicle()
        ));

        if (result.getType() == HitResult.Type.MISS
            && p.level().dimension().equals(safeTile.dimension())
            && TileHandler.getOwnedOrFriendedTiles(p)
                .contains(TileHandler.getTile(p.getServer(), safeTile.pos().getX(), safeTile.pos().getZ(), safeTile.dimension()))) {

            Vec3 offset = new Vec3(
                safeTile.pos().getX() - standing.getX(),
                p.getVehicle() == null
                    ? safeTile.pos().getY() - standing.getY()
                    : safeTile.pos().getY() - standing.getY() - 1,
                safeTile.pos().getZ() - standing.getZ()
            );

            Vec3 pushVec = offset.scale(0.1);
            if (p.getVehicle() != null) {
                p.getVehicle().push(pushVec.x, pushVec.y, pushVec.z);
                p.getVehicle().hurtMarked = true;
                return;
            }

            p.push(pushVec.x, pushVec.y, pushVec.z);
            p.hurtMarked = true;
        } else {
            GlobalPos pos = PlayerDataHandler.getLastSafeTile(p);
            p.teleportTo(
                p.getServer().getLevel(pos.dimension()),
                pos.pos().above().getX(),
                pos.pos().above().getY(),
                pos.pos().above().getZ(),
                p.getYRot(),
                p.getXRot()
            );
        }
    }
}
