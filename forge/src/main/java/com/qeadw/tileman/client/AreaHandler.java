package com.qeadw.tileman.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qeadw.tileman.client.renderer.BorderRenderer;
import com.qeadw.tileman.network.NetworkHandler;
import com.qeadw.tileman.network.packets.ClearSelectedTilesC2SPacket;
import com.qeadw.tileman.network.packets.SendSelectedTileC2SPacket;
import com.qeadw.tileman.tile.OwnedTile;
import com.qeadw.tileman.tile.Tile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class AreaHandler {
    private static OwnedTile block1;
    private static OwnedTile block2;
    public static ArrayList<OwnedTile> selectedArea = new ArrayList<>();
    private static long lastTimeUsed = 0L;
    private static final long COOLDOWN = 150L;

    public static void areaSelected() {
        if (System.currentTimeMillis() - lastTimeUsed < COOLDOWN) {
            return;
        }
        lastTimeUsed = System.currentTimeMillis();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        HitResult hitResult = player.pick(10.0, 0.0F, true);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        int chunkX = blockHitResult.getBlockPos().getX() >> 4;
        int chunkZ = blockHitResult.getBlockPos().getZ() >> 4;

        OwnedTile hit = BorderRenderer.getTile(chunkX, chunkZ, player.level().dimension());

        if (hit == null) {
            return;
        }

        if (block1 == null && block2 == null) {
            // First selection
            block1 = hit;
            if (block1.getOwner().equals(player.getUUID())) {
                NetworkHandler.CHANNEL.sendToServer(new SendSelectedTileC2SPacket(block1));
                selectedArea.add(block1);
            }
        } else if (block1 != null && block2 == null) {
            // Second selection - select area
            block2 = hit;
            getOwnedTilesInArea(block1, block2).forEach(tile -> {
                NetworkHandler.CHANNEL.sendToServer(new SendSelectedTileC2SPacket(tile));
                selectedArea.add(tile);
            });
        } else {
            // Third selection - clear
            block1 = null;
            block2 = null;
            selectedArea.clear();
            NetworkHandler.CHANNEL.sendToServer(new ClearSelectedTilesC2SPacket());
        }
    }

    private static ArrayList<OwnedTile> getOwnedTilesInArea(Tile pos1, Tile pos2) {
        ArrayList<OwnedTile> positions = new ArrayList<>();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return positions;

        int minX = Math.min(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                OwnedTile tile = BorderRenderer.getTile(x, z, pos1.getDimension());
                if (tile != null && tile.isOwner(player.getUUID())) {
                    positions.add(tile);
                }
            }
        }

        return positions;
    }

    public static void renderSelectedArea(PoseStack poseStack, float partialTick) {
        BorderRenderer.renderGroup(selectedArea, poseStack, Color.GREEN, partialTick);
    }

    public static void clearSelection() {
        block1 = null;
        block2 = null;
        selectedArea.clear();
    }
}
