package com.whosalbercik.tileman.client.renderer;

import com.whosalbercik.tileman.tile.OwnedTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

/**
 * Holds the visual options of a rendered {@link OwnedTile}
 */
public class BorderStyle {

    /**
     * Instantiates a new Border style.
     *
     * @param color colour of the tiles borders
     * @param tile  the tile
     */
    public BorderStyle(Color color, OwnedTile tile) {
        this.color = color;
        this.yPos = calculateY(tile);
    }

    /**
     * <p>{@link OwnedTile} only holds x and z coordinates, because y is not relevant</p>
     * <p>During rendering, this is a problem because the {@link BorderRenderer} does not know onto which Y coordinate to render</p>
     * <p>Hence why, a Y coordinate is calculated here</p>
     */
    public double yPos;
    /**
     * The colour of the {@link OwnedTile} borders.
     */
    public Color color;

    /**
     *  <p>These four variables hold if a border of this {@link OwnedTile} should be rendered or not</p>
     *  <p>Look into {@link com.whosalbercik.tileman.client.ClientTileHandler} for more information</p>
     */
    public boolean north = false;
    public boolean south = false;
    public boolean west = false;
    public boolean east = false;

    /**
     * Calculate the Y coordinate on which the border will be rendered.
     *
     * @param tile the tile
     * @return the Y coordinate on which the tile can be safely rendered
     */
    protected static double calculateY(OwnedTile tile) {
        LocalPlayer p = Minecraft.getInstance().player;
        p.level().getProfiler().push("calculateY");

        Vec3 possibleGround = new Vec3(tile.getX(), p.getBlockY() + 1, tile.getZ());

        // find close solid block
        while (!p.level().getBlockState(BlockPos.containing(possibleGround)).isSolidRender(p.level(), BlockPos.containing(possibleGround))) {
            possibleGround = possibleGround.subtract(new Vec3(0, 1, 0));

            // 5 blocks under player is limit
            if (p.blockPosition().getY() - possibleGround.y > 5) {
                break;
            }
        }
        // render border on the solid block
        possibleGround = possibleGround.add(0, 1, 0);
        p.level().getProfiler().pop();
        return possibleGround.y;

    }

    /**
     * Run during rendering to ensure correct Y position
     *
     * @param tile the tile
     */
    public void updateY(OwnedTile tile) {
        yPos = calculateY(tile);
    }

    /**
     * <p>Use this method to determine if this tile is renderable or not</p>
     * <p>If none of the borders have to be rendered, then the tile will not be added to rendering, as there's nothing to render</p>
     * @return renderable or not
     */
    public boolean isRenderable() {
        return north || south || west || east;
    }

}
