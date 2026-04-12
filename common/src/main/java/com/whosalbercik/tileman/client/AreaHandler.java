package com.whosalbercik.tileman.client;

import com.whosalbercik.tileman.LoaderServices;
import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import com.whosalbercik.tileman.client.renderer.BorderStyle;
import com.whosalbercik.tileman.networking.packet.SendSelectedTilesC2S;
import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Client-side handling for selecting areas
 */
public class AreaHandler {
    private static OwnedTile block1;
    private static OwnedTile block2;

    private static final HashMap<GlobalPos, OwnedTile> selectedArea = new HashMap<>();
    
    private static long lastTimeUsed = 0;

    /**
     * Run when player presses hotkey to select an area.
     * <p>Handles selecting and sends selected tiles to server via {@link SendSelectedTilesC2S}</p>
     */
    public static void areaSelected() {
        // cooldown
        long cooldown = 150;
        if (System.currentTimeMillis() - lastTimeUsed < cooldown) return;

        lastTimeUsed = System.currentTimeMillis();

        LocalPlayer p = Minecraft.getInstance().player;

        // raycast where looking
        HitResult hitResult = p.pick(10f, 0.0f, true);
        // if not hit
        if (hitResult.getLocation() == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;

        // get hit tile
        OwnedTile hit = ClientTileHandler.getTile(blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getZ(), p.level().dimension());

        // if hit but not hit tile
        if (hit == null) return;


        // selected first block
        if (block1 == null && block2 == null) {
            block1 = hit;
            if (block1.getOwner().equals(p.getUUID())) {
                selectedArea.put(block1.getGlobalPos(), block1);
                LoaderServices.PLATFORM.sendC2S(new SendSelectedTilesC2S(new HashSet<OwnedTile>(selectedArea.values())));
            }
        // selected second block
        } else if (block1 != null && block2 == null) {
            block2 = hit;

            getOwnedTilesInArea(block1, block2).forEach((tile -> {
                selectedArea.put(tile.getGlobalPos(), tile);
            }));

            LoaderServices.PLATFORM.sendC2S(new SendSelectedTilesC2S(new HashSet<OwnedTile>(selectedArea.values())));

            // clear selection
        } else if (block1 != null && block2 != null) {
            block2 = null;
            block1 = null;
            selectedArea.clear();

            BorderRenderer.clearTiles();
            ClientTileHandler.setDirty();

            LoaderServices.PLATFORM.sendC2S(new SendSelectedTilesC2S());
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
                if (ClientTileHandler.getTile(x, z, pos1.getDimension()) != null && ClientTileHandler.getTile(x, z, pos1.getDimension()).isOwner(Minecraft.getInstance().player.getUUID())) positions.add(ClientTileHandler.getTile(x, z, pos1.getDimension()));
            }
        }

        return positions;
    }
}
