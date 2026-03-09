package com.whosalbercik.tileman.client;


import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import com.whosalbercik.tileman.client.renderer.ClientTileHandler;
import com.whosalbercik.tileman.client.renderer.SidePanelRenderer;
import com.whosalbercik.tileman.networking.*;
import me.x150.renderer.event.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;


@Environment(EnvType.CLIENT)
public class TilemanClient implements ClientModInitializer {

    public static KeyBinding setArea = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.tileman.setArea",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.tileman.tileman"
    ));

    public static KeyBinding hideSidePanel = KeyBindingHelper.registerKeyBinding(new StickyKeyBinding(
            "key.tileman.hideSidePanel",
            GLFW.GLFW_KEY_I,
            "category.tileman.tileman",
            () -> true
    ));

    public static KeyBinding toggleAutoClaim = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.tileman.toggleAutoClaim",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_U,
        "category.tileman.tileman"
    ));

    private long lastAutoClaimToggleTime = 0;

    @Override
    public void onInitializeClient() {
        //RenderEvents.WORLD.register(ClientTileHandler::renderTiles);
        WorldRenderEvents.AFTER_ENTITIES.register(BorderRenderer::render);

        RenderEvents.HUD.register(SidePanelRenderer::render);

        ClientPlayNetworking.registerGlobalReceiver(SendTilesS2C.ID, ClientTileHandler::reveiveTileFromServer);
        ClientPlayNetworking.registerGlobalReceiver(ClearRenderedTilesS2C.ID, ClientTileHandler::clearRenderedTiles);
        ClientPlayNetworking.registerGlobalReceiver(SendSidePanelDataS2C.ID, SidePanelRenderer::setData);
        ClientPlayNetworking.registerGlobalReceiver(SendFriendsS2C.ID, ClientTileHandler::setFriends);

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (setArea.isPressed()) {
                AreaHandler.areaSelected();
            }

            long currentTime = System.currentTimeMillis();
            if (toggleAutoClaim.wasPressed() && (currentTime - lastAutoClaimToggleTime) >= 300) {
                lastAutoClaimToggleTime = currentTime;
                ClientConfig.toggleAutoClaim();
                ClientPlayNetworking.send(new SetTileAutoClaimC2S(ClientConfig.getAutoClaimEnabled()));
            }
        });
    }
}
