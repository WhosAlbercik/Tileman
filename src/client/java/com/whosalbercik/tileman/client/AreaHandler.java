package com.whosalbercik.tileman.client;

import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class AreaHandler {
    private static OwnedTile block1;
    private static OwnedTile block2;

    public static ArrayList<OwnedTile> selectedArea = new ArrayList<>();

    private static long lastTimeUsed = 0;
    private static long cooldown = 150;

    public static void areaSelected() {
        if (System.currentTimeMillis() - lastTimeUsed < cooldown) return;

        lastTimeUsed = System.currentTimeMillis();

        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        HitResult hitResult = p.raycast(10f, 0.0f, true);

        if (hitResult.getPos() == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        OwnedTile hit = BorderRenderer.getTile(blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getZ(), p.getWorld().getRegistryKey());

        if (hit == null) return;

        if (block1 == null && block2 == null) {
            block1 = hit;
            selectedArea = block1.getOwner().equals(p.getUuid()) ? new ArrayList<>(List.of(new OwnedTile[]{block1})) : new ArrayList<>();
        } else if (block1 != null && block2 == null) {
            block2 = hit;
            selectedArea = getOwnedTilesInArea(block1, block2);
        } else if (block1 != null && block2 != null) {
            block2 = null;
            block1 = null;
            selectedArea.clear();
        }
    }


    private static ArrayList<OwnedTile> getOwnedTilesInArea(Tile pos1, Tile pos2) {
        ArrayList<OwnedTile> positions = new ArrayList<>();

        int minX = Math.min(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (BorderRenderer.getTile(x, z, pos1.getDimension()) != null && BorderRenderer.getTile(x, z, pos1.getDimension()).isOwner(MinecraftClient.getInstance().player.getUuid())) positions.add(BorderRenderer.getTile(x, z, pos1.getDimension()));
            }
        }

        return positions;
    }

    public static void renderSelectedArea(MatrixStack stack) {
        BorderRenderer.renderGroup(selectedArea, stack, Color.GREEN);
    }
}
