package com.qeadw.tileman.tile;

import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class Tile {
    private final int x;
    private final int z;
    private final ResourceKey<Level> dimension;

    public Tile(int x, int z, ResourceKey<Level> dimension) {
        this.x = x;
        this.z = z;
        this.dimension = dimension;
    }

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

    public ResourceKey<Level> getDimension() {
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
        return this.x == pos.pos().getX() && this.z == pos.pos().getZ() && pos.dimension().equals(this.dimension);
    }

    public Tile north() {
        return new Tile(this.x, this.z - 1, this.dimension);
    }

    public Tile south() {
        return new Tile(this.x, this.z + 1, this.dimension);
    }

    public Tile west() {
        return new Tile(this.x - 1, this.z, this.dimension);
    }

    public Tile east() {
        return new Tile(this.x + 1, this.z, this.dimension);
    }
}
