package com.whosalbercik.tileman.tile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Used when a tile has an owner. when used in calculations and when the owner is irrelevant use Tile
 */
public class OwnedTile extends Tile{
    private UUID owner;

    public static PacketCodec<ByteBuf, OwnedTile>  PACKET_CODEC = new PacketCodec<ByteBuf, OwnedTile>() {
        public OwnedTile decode(ByteBuf byteBuf) {
            PacketByteBuf packetByteBuf = new PacketByteBuf(byteBuf);


            BlockPos pos = packetByteBuf.readBlockPos();
            RegistryKey<World> dimension = packetByteBuf.readNullable(RegistryKey.createPacketCodec(RegistryKeys.WORLD));

            String ownersString = StringEncoding.decode(byteBuf, 1000);


            return new OwnedTile(pos.getX(), pos.getZ(), dimension, UUID.fromString(ownersString));
        }

        public void encode(ByteBuf byteBuf, OwnedTile ownedTile) {
            PacketByteBuf packetByteBuf = new PacketByteBuf(byteBuf);

            packetByteBuf = packetByteBuf.writeBlockPos(new BlockPos(ownedTile.getX(), 0, ownedTile.getZ()));
            packetByteBuf.writeNullable(ownedTile.getDimension(), RegistryKey.createPacketCodec(RegistryKeys.WORLD));


            StringEncoding.encode(byteBuf, ownedTile.owner.toString(), 1000);

        }
    };


    protected OwnedTile(int x, int z, RegistryKey<World> dimension, PlayerEntity owner) {
        super(x, z, dimension);
        this.owner = owner.getUuid();
    }

    public OwnedTile(int x, int z, RegistryKey<World> dimension, UUID owner) {
        super(x, z, dimension);
        this.owner = owner;
    }

    protected OwnedTile(GlobalPos pos, PlayerEntity owner) {
        super(pos);
        this.owner = owner.getUuid();
    }

    public boolean isOwner(UUID player) {
        return owner.equals(player);
    }

    public UUID getOwner() {
        return owner;
    }

    public void transferOwnership(UUID newOwner) {
        this.owner = newOwner;
    }
}
