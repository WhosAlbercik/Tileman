package com.qeadw.tileman.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class OwnedTile extends Tile {
    private UUID owner;

    public OwnedTile(int x, int z, ResourceKey<Level> dimension, Player owner) {
        super(x, z, dimension);
        this.owner = owner.getUUID();
    }

    public OwnedTile(int x, int z, ResourceKey<Level> dimension, UUID owner) {
        super(x, z, dimension);
        this.owner = owner;
    }

    public OwnedTile(GlobalPos pos, Player owner) {
        super(pos);
        this.owner = owner.getUUID();
    }

    public boolean isOwner(UUID player) {
        return this.owner.equals(player);
    }

    public UUID getOwner() {
        return owner;
    }

    public void transferOwnership(UUID newOwner) {
        this.owner = newOwner;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(new BlockPos(getX(), 0, getZ()));
        buf.writeResourceLocation(getDimension().location());
        buf.writeUUID(owner);
    }

    public static OwnedTile fromBytes(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation dimLocation = buf.readResourceLocation();
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLocation);
        UUID owner = buf.readUUID();
        return new OwnedTile(pos.getX(), pos.getZ(), dimension, owner);
    }
}
