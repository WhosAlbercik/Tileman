package com.qeadw.tileman.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.qeadw.tileman.Tileman;
import com.qeadw.tileman.client.renderer.BorderRenderer;
import com.qeadw.tileman.client.renderer.SidePanelRenderer;
import com.qeadw.tileman.client.screen.TilemanSettingsScreen;
import com.qeadw.tileman.network.NetworkHandler;
import com.qeadw.tileman.network.packets.SetTileAutoClaimC2SPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Tileman.MODID, value = Dist.CLIENT)
public class TilemanClient {
    public static final String KEY_CATEGORY = "category.tileman.tileman";

    public static KeyMapping setAreaKey;
    public static KeyMapping hideSidePanelKey;
    public static KeyMapping toggleAutoClaimKey;

    private static long lastAutoClaimToggleTime = 0L;
    private static boolean sidePanelHidden = false;

    @Mod.EventBusSubscriber(modid = Tileman.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            setAreaKey = new KeyMapping(
                "key.tileman.setArea",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY
            );

            hideSidePanelKey = new KeyMapping(
                "key.tileman.hideSidePanel",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                KEY_CATEGORY
            );

            toggleAutoClaimKey = new KeyMapping(
                "key.tileman.toggleAutoClaim",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                KEY_CATEGORY
            );

            event.register(setAreaKey);
            event.register(hideSidePanelKey);
            event.register(toggleAutoClaimKey);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (setAreaKey != null && setAreaKey.consumeClick()) {
            AreaHandler.areaSelected();
        }

        if (hideSidePanelKey != null && hideSidePanelKey.consumeClick()) {
            sidePanelHidden = !sidePanelHidden;
        }

        if (toggleAutoClaimKey != null && toggleAutoClaimKey.isDown()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAutoClaimToggleTime >= 300L) {
                lastAutoClaimToggleTime = currentTime;
                ClientConfig.toggleAutoClaim();
                NetworkHandler.CHANNEL.sendToServer(new SetTileAutoClaimC2SPacket(ClientConfig.getAutoClaimEnabled()));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        BorderRenderer.renderTiles(event.getPoseStack(), event.getPartialTick());
        AreaHandler.renderSelectedArea(event.getPoseStack(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (!sidePanelHidden) {
            SidePanelRenderer.render(event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen pauseScreen) {
            int x = pauseScreen.width / 7 - 50;
            int y = pauseScreen.height / 10;
            event.addListener(Button.builder(Component.literal("Tileman Settings"), button -> {
                Minecraft.getInstance().setScreen(new TilemanSettingsScreen(pauseScreen));
            }).bounds(x, y, 100, 20).build());
        }
    }

    public static boolean isSidePanelHidden() {
        return sidePanelHidden;
    }
}
