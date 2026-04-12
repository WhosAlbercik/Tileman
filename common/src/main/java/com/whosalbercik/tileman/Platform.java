package com.whosalbercik.tileman;

import com.whosalbercik.tileman.networking.Packet;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

/**
 * Platform Interface. Classes in common module use objects implementing this interface to invoke platform specific methods
 */
public interface Platform {

    /**
     * Send packet from Server to Client
     *
     * @param client the client
     * @param packet the packet
     */
    void sendS2C(ServerPlayer client, Packet packet);

    /**
     * Send packet from Client to Server
     *
     * @param packet the packet
     */
    void sendC2S(Packet packet);

    /**
     * Implement this and return Config directory.
     *
     * @return config directory
     */
    Path getConfigDir();

}
