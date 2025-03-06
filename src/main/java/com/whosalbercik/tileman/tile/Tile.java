package com.whosalbercik.tileman.tile;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;



public class Tile {
    private int x;
    private int z;
    private RegistryKey<World> dimension;


    public Tile(int x, int z, RegistryKey<World> dimension) {
        this.x = x;
        this.z = z;
        this.dimension = dimension;
    }

    /**
     *
     * @param pos position of the tile, y is ignored
     */
    public Tile(GlobalPos pos) {
        this.x = pos.pos().getX();
        this.z = pos.pos().getZ();
        this.dimension = pos.dimension();
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public RegistryKey<World> getDimension() {
        return dimension;
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

    public boolean equals(GlobalPos pos) {
        return x == pos.pos().getX() && z == pos.pos().getZ() && pos.dimension().equals(this.dimension);
    }

    public Tile north() {
        return new Tile(x, z - 1, dimension);
    }

    public Tile south() {
        return new Tile(x, z + 1, dimension);
    }

    public Tile west() {
        return new Tile(x - 1, z, dimension);
    }

    public Tile east() {
        return new Tile(x + 1, z, dimension);
    }
}
