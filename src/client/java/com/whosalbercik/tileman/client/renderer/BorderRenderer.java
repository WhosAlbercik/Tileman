package com.whosalbercik.tileman.client.renderer;

import com.whosalbercik.tileman.client.ClientConfig;
import com.whosalbercik.tileman.tile.OwnedTile;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;

import java.awt.*;
import java.util.HashMap;

public class BorderRenderer {

    /*
        Renderable are tiles that are at borders of the area
        This stops looping through tiles that do not need to be rendered
     */
    private static HashMap<OwnedTile, BorderStyle> renderableTiles = new HashMap<>();

    public static void putTile(OwnedTile tile, BorderStyle style) {
        renderableTiles.put(tile, style);
    }

    public static void clearTiles() {
        renderableTiles.clear();
    }
    public static void removeTile(OwnedTile tile) {
        renderableTiles.remove(tile);
    }

    public static void render(WorldRenderContext ctx) {
        Profilers.get().push("renderBorders");

        if (ClientTileHandler.isDirty) {
            ClientTileHandler.rebuildAll();
        }

        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        ctx.matrixStack().push();

        VertexConsumer consumer = ctx.consumers().getBuffer(RenderLayer.getDebugQuads());

        for (OwnedTile tile : renderableTiles.keySet()) {
            // if tile is outside render distance or is in a different dimension
            if (!new BlockPos(tile.getX(), (int) p.getY(), tile.getZ()).isWithinDistance(p.getBlockPos(), ClientConfig.getBorderRenderDistance()) || !p.getWorld().getRegistryKey().equals(tile.getDimension())) {
                continue;
            }

            // TODO: think of better way to do ts
            BorderStyle style = renderableTiles.get(tile);
            style.updateY(tile);

            if (style.north) addNorth(tile, renderableTiles.get(tile), ctx.matrixStack(), consumer);
            if (style.south) addSouth(tile, renderableTiles.get(tile), ctx.matrixStack(), consumer);
            if (style.east) addEast(tile, renderableTiles.get(tile), ctx.matrixStack(), consumer);
            if (style.west) addWest(tile, renderableTiles.get(tile), ctx.matrixStack(), consumer);
        }
        Profilers.get().pop();

        ctx.matrixStack().pop();
    }

    private static void addNorth(OwnedTile tile, BorderStyle style, MatrixStack positionMatrix, VertexConsumer consumer) {
        Vec3d pos = new Vec3d(tile.getX(), style.yPos, tile.getZ());

        addFace(pos, new Vec3d(1, 0, 0.1), style, positionMatrix, consumer);

    }

    private static void addSouth(OwnedTile tile, BorderStyle style, MatrixStack positionMatrix, VertexConsumer consumer) {
        Vec3d pos = new Vec3d(tile.getX(), style.yPos, tile.getZ() + 1);

        addFace(pos, new Vec3d(1, 0, -0.1), style, positionMatrix, consumer);

    }

    private static void addWest(OwnedTile tile, BorderStyle style, MatrixStack positionMatrix, VertexConsumer consumer) {
        Vec3d pos = new Vec3d(tile.getX(), style.yPos, tile.getZ());

        addFace(pos, new Vec3d(0.1, 0, 1), style, positionMatrix, consumer);

    }

    private static void addEast(OwnedTile tile, BorderStyle style, MatrixStack positionMatrix, VertexConsumer consumer) {
        Vec3d pos = new Vec3d(tile.getX() + 1, style.yPos, tile.getZ());

        addFace(pos, new Vec3d(-0.1, 0, 1), style, positionMatrix, consumer);

    }

    private static void addFace(Vec3d worldStart, Vec3d dimensions, BorderStyle style, MatrixStack positionMatrix, VertexConsumer consumer) {
        MatrixStack.Entry matrix = positionMatrix.peek();

        Vec3d start = transformVec3d(worldStart.add(0, 0.01, 0));
        Vec3d end = start.add(dimensions);

        Color color = style.color;

        consumer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z).color(rgbToArgb(color.getRGB()));
        consumer.vertex(matrix, (float) end.x, (float) start.y, (float) start.z).color(rgbToArgb(color.getRGB()));
        consumer.vertex(matrix, (float) end.x, (float) start.y, (float) end.z).color(rgbToArgb(color.getRGB()));
        consumer.vertex(matrix, (float) start.x, (float) start.y, (float) end.z).color(rgbToArgb(color.getRGB()));
    }

    private static Vec3d transformVec3d(Vec3d in) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        return in.subtract(camPos);
    }

    public static int rgbToArgb(int rgb) {
        return 0xFF000000 | rgb;
    }
}