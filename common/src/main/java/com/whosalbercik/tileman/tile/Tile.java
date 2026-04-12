package com.whosalbercik.tileman.tile;


import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Holds information regarding a tiles position in the world
 */
public class Tile {
    private int x;
    private int z;
    private ResourceKey<Level> dimension;


    /**
     * Instantiates a new Tile.
     *
     * @param x         the x
     * @param z         the z
     * @param dimension the dimension
     */
    public Tile(int x, int z, ResourceKey<Level> dimension) {
        this.x = x;
        this.z = z;
        this.dimension = dimension;
    }

    /**
     * Instantiates a new Tile.
     *
     * @param pos position of the tile, y is ignored
     */
    public Tile(GlobalPos pos) {
        this.x = pos.pos().getX();
        this.z = pos.pos().getZ();
        this.dimension = pos.dimension();
    }

    /**
     * Gets x.
     *
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * Gets z.
     *
     * @return the z
     */
    public int getZ() {
        return z;
    }

    /**
     * Gets dimension.
     *
     * @return the dimension
     */
    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    /**
     * Gets the global pos. Y is always equals 0
     *
     * @return Tile position as {@link GlobalPos}
     */
    public GlobalPos getGlobalPos() {
        return new GlobalPos(dimension, new BlockPos(x, 0, z));
    }

    @Override
    public int hashCode() {
        return 31 * x + z + dimension.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tile tile)) return false;
        return tile.x == this.x && tile.z == this.z && this.dimension.equals(tile.dimension);
    }

    /**
     * The tile touching the north border
     *
     * @return the north tile
     */
    public Tile north() {
        return new Tile(x, z - 1, dimension);
    }

    /**
     * The tile touching the south border
     *
     * @return the south tile
     */
    public Tile south() {
        return new Tile(x, z + 1, dimension);
    }

    /**
     * The tile touching the west border
     *
     * @return the west tile
     */
    public Tile west() {
        return new Tile(x - 1, z, dimension);
    }

    /**
     * The tile touching the east border
     *
     * @return the east tile
     */
    public Tile east() {
        return new Tile(x + 1, z, dimension);
    }
}
