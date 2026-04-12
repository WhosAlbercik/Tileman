package com.whosalbercik.tileman;

import com.mojang.blaze3d.platform.InputConstants;
import com.whosalbercik.tileman.client.AreaHandler;
import com.whosalbercik.tileman.client.ClientConfig;
import com.whosalbercik.tileman.client.ClientTileHandler;
import com.whosalbercik.tileman.networking.packet.SetTileAutoClaimC2S;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Events that should run client-side
 */
public class TilemanClientEvents {
    /**
     * Key Mappings
     */
    public static KeyMapping setArea = new KeyMapping(
            "key.tileman.setArea",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.tileman.tileman"
    );
    public static KeyMapping hideSidePanel = new ToggleKeyMapping(
            "key.tileman.hideSidePanel",
            GLFW.GLFW_KEY_I,
            "category.tileman.tileman",
            () -> true
    );
    public static KeyMapping toggleAutoClaim = new KeyMapping(
            "key.tileman.toggleAutoClaim",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "category.tileman.tileman"
    );

    private static long cooldown = 0;

    /**
     * Function should run every tick
     */
    public static void tickClient() {
        long currentTime = System.currentTimeMillis();

        if ((currentTime - cooldown) <= 100) return;

        if (setArea.isDown()) {
            AreaHandler.areaSelected();
            cooldown = currentTime;
        }

        if (toggleAutoClaim.isDown() && (currentTime - cooldown) >= 300) {
            ClientConfig.toggleAutoClaim();

            LoaderServices.PLATFORM.sendC2S(new SetTileAutoClaimC2S(ClientConfig.getAutoClaimEnabled()));
            cooldown = currentTime;
        }
    }

    /**
     * Function should run when client joins a new world
     */
    public static void clientJoin() {
        ClientTileHandler.clearAll();
    }
}
