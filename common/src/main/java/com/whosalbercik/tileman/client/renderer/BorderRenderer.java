package com.whosalbercik.tileman.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.whosalbercik.tileman.client.ClientConfig;
import com.whosalbercik.tileman.client.ClientTileHandler;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.HashMap;

/**
 * Renders the borders of tiles <p>
 *
 */
public class BorderRenderer  {

    private static final HashMap<OwnedTile, BorderStyle> renderableTiles = new HashMap<>();

    /**
     * Adds the tile to render its border
     *
     * @param tile  {@link OwnedTile}, which border is going to be rendered
     * @param style {@link BorderStyle} of the rendering
     */
    public static void putTile(OwnedTile tile, BorderStyle style) {
        renderableTiles.put(tile, style);
    }

    /**
     * Resets rendering
     */
    public static void clearTiles() {
        renderableTiles.clear();
    }

    /**
     * Remove tile from rendering
     *
     * @param tile removed tile
     */
    public static void removeTile(OwnedTile tile) {
        renderableTiles.remove(tile);
    }

    /**
     * Render borders of all added {@link OwnedTile}s in the matching {@link BorderStyle}
     *
     * @param poseStack the posestack
     * @param consumer the consumer
     */
    public static void render(PoseStack poseStack, VertexConsumer consumer) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) return;

        ProfilerFiller profiler = mc.getProfiler();

        profiler.push("renderBorders");

        if (ClientTileHandler.isDirty()) {
            ClientTileHandler.rebuildAll();
        }

        LocalPlayer p = mc.player;

        poseStack.pushPose();

        for (OwnedTile tile : renderableTiles.keySet()) {
            // if tile is outside render distance or is in a different dimension
            if (!new BlockPos(tile.getX(), (int) p.getY(), tile.getZ()).closerThan(p.blockPosition(), ClientConfig.getBorderRenderDistance()) || !p.level().dimension().equals(tile.getDimension())) {
                continue;
            }

            // TODO: think of better way to do ts
            BorderStyle style = renderableTiles.get(tile);
            style.updateY(tile);

            if (style.north) addNorth(tile, renderableTiles.get(tile), poseStack, consumer);
            if (style.south) addSouth(tile, renderableTiles.get(tile), poseStack, consumer);
            if (style.east) addEast(tile, renderableTiles.get(tile), poseStack, consumer);
            if (style.west) addWest(tile, renderableTiles.get(tile), poseStack, consumer);
        }
        profiler.pop();

        poseStack.popPose();
    }

    private static void addNorth(OwnedTile tile, BorderStyle style, PoseStack positionMatrix, VertexConsumer consumer) {
        Vec3 pos = new Vec3(tile.getX(), style.yPos, tile.getZ());

        addFace(pos, new Vec3(1, 0, 0.1), style, positionMatrix, consumer);

    }

    private static void addSouth(OwnedTile tile, BorderStyle style, PoseStack positionMatrix, VertexConsumer consumer) {
        Vec3 pos = new Vec3(tile.getX(), style.yPos, tile.getZ() + 1);

        addFace(pos, new Vec3(1, 0, -0.1), style, positionMatrix, consumer);

    }

    private static void addWest(OwnedTile tile, BorderStyle style, PoseStack positionMatrix, VertexConsumer consumer) {
        Vec3 pos = new Vec3(tile.getX(), style.yPos, tile.getZ());

        addFace(pos, new Vec3(0.1, 0, 1), style, positionMatrix, consumer);

    }

    private static void addEast(OwnedTile tile, BorderStyle style, PoseStack positionMatrix, VertexConsumer consumer) {
        Vec3 pos = new Vec3(tile.getX() + 1, style.yPos, tile.getZ());

        addFace(pos, new Vec3(-0.1, 0, 1), style, positionMatrix, consumer);

    }

    private static void addFace(Vec3 worldStart, Vec3 dimensions, BorderStyle style, PoseStack positionMatrix, VertexConsumer consumer) {
        PoseStack.Pose matrix = positionMatrix.last();

        Vec3 start = transformVec3d(worldStart.add(0, 0.01, 0));
        Vec3 end = start.add(dimensions);

        Color color = style.color;

        consumer.addVertex(matrix, (float) start.x, (float) start.y, (float) start.z).setColor(rgbToArgb(color.getRGB()));
        consumer.addVertex(matrix, (float) end.x, (float) start.y, (float) start.z).setColor(rgbToArgb(color.getRGB()));
        consumer.addVertex(matrix, (float) end.x, (float) start.y, (float) end.z).setColor(rgbToArgb(color.getRGB()));
        consumer.addVertex(matrix, (float) start.x, (float) start.y, (float) end.z).setColor(rgbToArgb(color.getRGB()));
    }

    private static Vec3 transformVec3d(Vec3 in) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        return in.subtract(camPos);
    }

    private static int rgbToArgb(int rgb) {
        return 0xFF000000 | rgb;
    }
}