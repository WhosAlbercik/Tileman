package com.whosalbercik.tileman.client;

import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import com.whosalbercik.tileman.client.renderer.BorderStyle;
import com.whosalbercik.tileman.client.renderer.ClientTileHandler;
import com.whosalbercik.tileman.networking.ClearSelectedTilesC2S;
import com.whosalbercik.tileman.networking.SendSelectedTileC2S;
import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.GlobalPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;


public class AreaHandler {
    private static OwnedTile block1;
    private static OwnedTile block2;

    public static HashMap<GlobalPos, OwnedTile> selectedArea = new HashMap<>();

    private static long lastTimeUsed = 0;
    private static long cooldown = 150;

    public static void areaSelected() {
        // cooldown
        if (System.currentTimeMillis() - lastTimeUsed < cooldown) return;

        lastTimeUsed = System.currentTimeMillis();

        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        // raycast where looking
        HitResult hitResult = p.raycast(10f, 0.0f, true);
        // if not hit
        if (hitResult.getPos() == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;

        // get hit tile
        OwnedTile hit = ClientTileHandler.getTile(blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getZ(), p.getWorld().getRegistryKey());

        // if hit but not hit tile
        if (hit == null) return;

        // selected first block
        if (block1 == null && block2 == null) {
            block1 = hit;
            if (block1.getOwner().equals(p.getUuid())) {
                ClientPlayNetworking.send(new SendSelectedTileC2S(block1));
                selectedArea.put(block1.getGlobalPos(), block1);
            }
        // selected second block
        } else if (block1 != null && block2 == null) {
            block2 = hit;
            getOwnedTilesInArea(block1, block2).forEach((tile -> {
                ClientPlayNetworking.send(new SendSelectedTileC2S(tile));
                selectedArea.put(tile.getGlobalPos(), tile);
            }));

            // clear selection
        } else if (block1 != null && block2 != null) {
            block2 = null;
            block1 = null;
            selectedArea.clear();

            BorderRenderer.clearTiles();
            ClientTileHandler.setDirty();

            ClientPlayNetworking.send(new ClearSelectedTilesC2S());
        }

        // rebuild area rendering
        for (OwnedTile selected : selectedArea.values()) {

            BorderStyle tileStyle = new BorderStyle(Color.green, selected);
            // if the west tile doesnt exist
            if (selectedArea.get(selected.west().getGlobalPos()) == null) {
                tileStyle.west = true;
            }
            if (selectedArea.get(selected.east().getGlobalPos()) == null) {
                tileStyle.east = true;
            }
            if (selectedArea.get(selected.north().getGlobalPos()) == null) {
                tileStyle.north = true;
            }
            if (selectedArea.get(selected.south().getGlobalPos()) == null) {
                tileStyle.south = true;
            }

            if (tileStyle.isRenderable()) {
                BorderRenderer.putTile(selected, tileStyle);
            } else {
                BorderRenderer.removeTile(selected);
            }
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
                if (ClientTileHandler.getTile(x, z, pos1.getDimension()) != null && ClientTileHandler.getTile(x, z, pos1.getDimension()).isOwner(MinecraftClient.getInstance().player.getUuid())) positions.add(ClientTileHandler.getTile(x, z, pos1.getDimension()));
            }
        }

        return positions;
    }
}
