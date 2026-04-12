package com.qeadw.tileman.client;

import com.qeadw.tileman.Tileman;
import com.whosalbercik.tileman.TilemanClientEvents;
import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import com.whosalbercik.tileman.client.screen.TilemanSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tileman.MODID, value = Dist.CLIENT)
public class TilemanClient {

    @Mod.EventBusSubscriber(modid = Tileman.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(TilemanClientEvents.hideSidePanel);
            event.register(TilemanClientEvents.setArea);
            event.register(TilemanClientEvents.toggleAutoClaim);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        TilemanClientEvents.tickClient();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderPlayerEvent event) {
        BorderRenderer.render(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.debugQuads()));
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
}
