package com.whosalbercik.tileman.client.renderer;

import com.whosalbercik.tileman.client.ClientConfig;
import com.whosalbercik.tileman.networking.ClearRenderedTilesS2C;
import com.whosalbercik.tileman.networking.SendFriendsS2C;
import com.whosalbercik.tileman.networking.SendTilesS2C;
import com.whosalbercik.tileman.tile.OwnedTile;
import com.whosalbercik.tileman.tile.Tile;
import me.x150.renderer.render.Renderer3d;
import me.x150.renderer.util.AlphaOverride;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.*;

@Environment(EnvType.CLIENT)
public class BorderRenderer {
    public static ArrayList<OwnedTile> tiles = new ArrayList<>();

    private static ArrayList<UUID> friends = new ArrayList<>();

    public static void renderTiles(MatrixStack stack) {

        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        if (p == null) return;

        ArrayList<Tile> owned = new ArrayList<>();
        ArrayList<Tile> stranger = new ArrayList<>();


        // Sort into lists based on owners
        for (OwnedTile ownedTile : tiles) {
            if (ownedTile.getOwner().equals(p.getUuid()) || friends.contains(ownedTile.getOwner()))
                owned.add(ownedTile);
            else stranger.add(ownedTile);
        }

        renderGroup(owned, stack, ClientConfig.getFriendlyBorder());
        renderGroup(stranger, stack, ClientConfig.getEnemyBorder());

    }
    public static void renderGroup(ArrayList<? extends Tile> group, MatrixStack stack, Color color) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        for (Tile tile: group) {

            if (!new BlockPos(tile.getX(), p.getBlockY(), tile.getZ()).isWithinDistance(p.getBlockPos(), ClientConfig.getBorderRenderDistance()) || !p.getWorld().getRegistryKey().equals(tile.getDimension())) continue;

            if (!group.contains(tile.west())) {
                renderWestBorder(tile, stack, p, color);
            }
            if (!group.contains(tile.east())) {
                renderEastBorder(tile, stack, p, color);
            }
            if (!group.contains(tile.north())) {
                renderNorthBorder(tile, stack, p, color);
            }
            if (!group.contains(tile.south())) {
                renderSouthBorder(tile, stack, p, color);
            }


        }
    }

    public static void renderNorthBorder(Tile tile, MatrixStack stack, ClientPlayerEntity player, Color color) {
        Vec3d possibleGround = new Vec3d(tile.getX(), player.getBlockY() + 2, tile.getZ());

        BlockHitResult result =  player.getWorld().raycast(new RaycastContext(possibleGround, possibleGround.subtract(0, 100, 0), RaycastContext.ShapeType.OUTLINE , RaycastContext.FluidHandling.SOURCE_ONLY, player));

        // TODO: Remove this and implement own functionality
        Renderer3d.renderFilled(stack, color, result.getPos(), new Vec3d(1f, 0.01, 0.1f));
    }

    public static void renderSouthBorder(Tile tile, MatrixStack stack, ClientPlayerEntity player, Color color) {
        Vec3d possibleGround = new Vec3d(tile.getX(), player.getBlockY() + 2, tile.getZ());

        BlockHitResult result =  player.getWorld().raycast(new RaycastContext(possibleGround, possibleGround.subtract(0, 100, 0), RaycastContext.ShapeType.OUTLINE , RaycastContext.FluidHandling.SOURCE_ONLY, player));


        Renderer3d.renderFilled(stack, color, result.getPos().add(0f, 0.01f, 1f), new Vec3d(1f, 0.01, -0.1f));
    }

    public static void renderWestBorder(Tile tile, MatrixStack stack, ClientPlayerEntity player, Color color) {
        Vec3d possibleGround = new Vec3d(tile.getX(), player.getBlockY() + 2, tile.getZ());

        BlockHitResult result =  player.getWorld().raycast(new RaycastContext(possibleGround, possibleGround.subtract(0, 100, 0), RaycastContext.ShapeType.OUTLINE , RaycastContext.FluidHandling.SOURCE_ONLY, player));

        Renderer3d.renderFilled(stack, color, result.getPos().add(0, 0.01f, 1f), new Vec3d(0.1f, 0.01, -1f));
    }

    public static void renderEastBorder(Tile tile, MatrixStack stack, ClientPlayerEntity player, Color color) {
        Vec3d possibleGround = new Vec3d(tile.getX(), player.getBlockY() + 2, tile.getZ());

        BlockHitResult result =  player.getWorld().raycast(new RaycastContext(possibleGround, possibleGround.subtract(0, 100, 0), RaycastContext.ShapeType.OUTLINE , RaycastContext.FluidHandling.SOURCE_ONLY, player));


        Renderer3d.renderFilled(stack, color, result.getPos().add(1, 0.03, 1), new Vec3d(-0.1f, 0.01, -1f));
    }




    public static void reveiveTileFromServer(SendTilesS2C sendTilesS2C, ClientPlayNetworking.Context context) {
        ArrayList<Tile> toBeRemoved = new ArrayList<>();

        for (Tile tile: tiles) {
            if (tile.equals(sendTilesS2C.tile())) {
                toBeRemoved.add(tile); // owners has changed
            }
        }

        tiles.removeAll(toBeRemoved);

        tiles.add(sendTilesS2C.tile());
    }

    public static void clearRenderedTiles(ClearRenderedTilesS2C clearRenderedTilesS2C, ClientPlayNetworking.Context context) {
        tiles.clear();
    }

    public static void setFriends(SendFriendsS2C sendFriendsS2C, ClientPlayNetworking.Context context) {
        String friendsString = sendFriendsS2C.friends();
        String[] friendsArray = friendsString.split(";");

        friends = new ArrayList<>();

        if (friendsArray[0].isEmpty()) return;

        for (String friendString: friendsArray) {
            friends.add(UUID.fromString(friendString));
        }

    }

    public static OwnedTile getTile(int x, int z, RegistryKey<World> dimension) {
        for (OwnedTile tile: tiles) {
            if (tile.getX() == x && tile.getZ() == z && tile.getDimension().equals(dimension)) return tile;
        }
        return null;
    }
}
