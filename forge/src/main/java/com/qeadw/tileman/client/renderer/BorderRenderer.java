package com.qeadw.tileman.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.qeadw.tileman.client.ClientConfig;
import com.qeadw.tileman.tile.OwnedTile;
import com.qeadw.tileman.tile.Tile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class BorderRenderer {
    public static ArrayList<OwnedTile> tiles = new ArrayList<>();
    private static ArrayList<UUID> friends = new ArrayList<>();

    public static void renderTiles(PoseStack poseStack, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ArrayList<Tile> owned = new ArrayList<>();
        ArrayList<Tile> stranger = new ArrayList<>();

        for (OwnedTile ownedTile : tiles) {
            if (!ownedTile.getOwner().equals(player.getUUID()) && !friends.contains(ownedTile.getOwner())) {
                stranger.add(ownedTile);
            } else {
                owned.add(ownedTile);
            }
        }

        renderGroup(owned, poseStack, ClientConfig.getFriendlyBorder(), partialTick);
        renderGroup(stranger, poseStack, ClientConfig.getEnemyBorder(), partialTick);
    }

    public static void renderGroup(ArrayList<? extends Tile> group, PoseStack poseStack, Color color, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        for (Tile tile : group) {
            // Tiles are block-based (1x1 blocks)
            int tileBlockX = tile.getX();
            int tileBlockZ = tile.getZ();

            BlockPos tilePos = new BlockPos(tileBlockX, (int) player.getY(), tileBlockZ);

            if (tilePos.closerThan(player.blockPosition(), ClientConfig.getBorderRenderDistance()) &&
                player.level().dimension().equals(tile.getDimension())) {

                if (!group.contains(tile.west())) {
                    renderWestBorder(tile, poseStack, camPos, color);
                }
                if (!group.contains(tile.east())) {
                    renderEastBorder(tile, poseStack, camPos, color);
                }
                if (!group.contains(tile.north())) {
                    renderNorthBorder(tile, poseStack, camPos, color);
                }
                if (!group.contains(tile.south())) {
                    renderSouthBorder(tile, poseStack, camPos, color);
                }
            }
        }
    }

    private static void renderFilledBox(PoseStack poseStack, Vec3 camPos, Vec3 start, Vec3 dimensions, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        float x1 = (float) (start.x - camPos.x);
        float y1 = (float) (start.y - camPos.y);
        float z1 = (float) (start.z - camPos.z);
        float x2 = (float) (x1 + dimensions.x);
        float y2 = (float) (y1 + dimensions.y);
        float z2 = (float) (z1 + dimensions.z);

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = 0.7f;

        // Bottom face
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();

        // Top face
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();

        // Front face (north, -Z)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();

        // Back face (south, +Z)
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();

        // Left face (west, -X)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();

        // Right face (east, +X)
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderNorthBorder(Tile tile, PoseStack poseStack, Vec3 camPos, Color color) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int x = tile.getX();
        int z = tile.getZ();
        double y = player.getY() - 0.5;

        Vec3 start = new Vec3(x, y, z);
        Vec3 size = new Vec3(1, 0.15, 0.1);
        renderFilledBox(poseStack, camPos, start, size, color);
    }

    public static void renderSouthBorder(Tile tile, PoseStack poseStack, Vec3 camPos, Color color) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int x = tile.getX();
        int z = tile.getZ() + 1;
        double y = player.getY() - 0.5;

        Vec3 start = new Vec3(x, y, z - 0.1);
        Vec3 size = new Vec3(1, 0.15, 0.1);
        renderFilledBox(poseStack, camPos, start, size, color);
    }

    public static void renderWestBorder(Tile tile, PoseStack poseStack, Vec3 camPos, Color color) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int x = tile.getX();
        int z = tile.getZ();
        double y = player.getY() - 0.5;

        Vec3 start = new Vec3(x, y, z);
        Vec3 size = new Vec3(0.1, 0.15, 1);
        renderFilledBox(poseStack, camPos, start, size, color);
    }

    public static void renderEastBorder(Tile tile, PoseStack poseStack, Vec3 camPos, Color color) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int x = tile.getX() + 1;
        int z = tile.getZ();
        double y = player.getY() - 0.5;

        Vec3 start = new Vec3(x - 0.1, y, z);
        Vec3 size = new Vec3(0.1, 0.15, 1);
        renderFilledBox(poseStack, camPos, start, size, color);
    }

    public static void receiveTileFromServer(OwnedTile tile) {
        tiles.removeIf(t -> t.equals(tile));
        tiles.add(tile);
    }

    public static void clearRenderedTiles() {
        tiles.clear();
    }

    public static void setFriends(String friendsString) {
        String[] friendsArray = friendsString.split(";");
        friends = new ArrayList<>();
        if (!friendsArray[0].isEmpty()) {
            for (String friendString : friendsArray) {
                friends.add(UUID.fromString(friendString));
            }
        }
    }

    public static OwnedTile getTile(int x, int z, ResourceKey<Level> dimension) {
        for (OwnedTile tile : tiles) {
            if (tile.getX() == x && tile.getZ() == z && tile.getDimension().equals(dimension)) {
                return tile;
            }
        }
        return null;
    }
}
