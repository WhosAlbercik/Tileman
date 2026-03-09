package com.whosalbercik.tileman.client.renderer;

import com.whosalbercik.tileman.tile.Tile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;

import java.awt.*;

public class BorderStyle {

    public BorderStyle(Color color, Tile tile) {
        this.color = color;
        this.yPos = calculateY(tile);
    }

    // y position where border is drawn
    public double yPos;
    public Color color;

    // which sides of the tile should be rendered
    public boolean north = false;
    public boolean south = false;
    public boolean west = false;
    public boolean east = false;

    protected static double calculateY(Tile tile) {
        Profilers.get().push("calculateY");
        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        Vec3d possibleGround = new Vec3d(tile.getX(), p.getBlockY() + 1, tile.getZ());

        // find close solid block
        while (!p.getWorld().getBlockState(BlockPos.ofFloored(possibleGround)).isSolidBlock(p.getWorld(), BlockPos.ofFloored(possibleGround))) {
            possibleGround = possibleGround.subtract(new Vec3d(0, 1, 0));

            // 5 blocks under player is limit
            if (p.getBlockPos().getY() - possibleGround.getY() > 5) {
                break;
            }
        }
        // render border on the solid block
        possibleGround = possibleGround.add(0, 1, 0);
        Profilers.get().pop();
        return possibleGround.y;

    }

    public void updateY(Tile tile) {
        yPos = calculateY(tile);
    }

    public boolean isRenderable() {
        return north || south || west || east;
    }

}
