package com.whosalbercik.tileman.networking.packet;

import com.whosalbercik.tileman.client.renderer.SidePanelRenderer;
import com.whosalbercik.tileman.networking.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.rmi.NotBoundException;


/**
 * Sends information that is displayed in the SidePanel
 */
public class SendSidePanelDataS2C extends Packet {
    /**
     * Encodes and decodes two {@link Integer}
     */
    public static final StreamCodec<FriendlyByteBuf, SendSidePanelDataS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SendSidePanelDataS2C::getAvailableTiles,
            ByteBufCodecs.INT, SendSidePanelDataS2C::getUnlockedTiles,
            SendSidePanelDataS2C::new
    );
    public static final ResourceLocation ID = ResourceLocation.tryBuild("tileman", "send_side_panel");
    public static final Type<SendSidePanelDataS2C> TYPE = new Type<>(ID);


    public final int availableTiles;
    public final int unlockedTiles;

    public SendSidePanelDataS2C(int availableTiles, int unlockedTiles) {
        this.availableTiles = availableTiles;
        this.unlockedTiles = unlockedTiles;
    }

    public int getAvailableTiles() {
        return availableTiles;
    }

    public int getUnlockedTiles() {
        return unlockedTiles;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void clientHandle() throws NotBoundException {
        SidePanelRenderer.availableTiles = availableTiles;
        SidePanelRenderer.unlockedTiles = unlockedTiles;
    }
}
