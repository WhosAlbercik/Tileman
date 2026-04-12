package com.qeadw.tileman.client;

import com.qeadw.tileman.Tileman;
import com.whosalbercik.tileman.client.renderer.SidePanelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tileman.MODID, value = Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
public class BusEvents {

    @SubscribeEvent
    public static void onRenderGui(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(
                ResourceLocation.fromNamespaceAndPath("tileman", "side_panel"),
                new SidePanelRenderer());

    }

}
