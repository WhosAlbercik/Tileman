package com.whosalbercik.tileman.networking;


import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.rmi.NotBoundException;

/**
 * All packets should implement this class. This is to ensure compatibility between Forge and Fabric packets.
 * Override appropriate methods to implement functionality and declare direction. See {@link com.whosalbercik.tileman.Platform} for details.
 */
public abstract class Packet implements CustomPacketPayload {

    // Implement these methods to define direction
    public void serverHandle(ServerPlayer origin) throws NotBoundException {
        throw new NotBoundException("Packet Executed in wrong direction");
    }

    public void clientHandle() throws NotBoundException {
        throw new NotBoundException("Packet Executed in wrong direction");
    }
}
