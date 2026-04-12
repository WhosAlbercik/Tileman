package com.qeadw.tileman;

import com.qeadw.tileman.network.ForgePlatform;
import com.whosalbercik.tileman.LoaderServices;
import com.whosalbercik.tileman.TilemanCommands;
import com.whosalbercik.tileman.TilemanEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Tileman.MODID)
public class Tileman {
    public static final String MODID = "tileman";

    static {
        LoaderServices.PLATFORM = new ForgePlatform();
    }

    public Tileman() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ForgePlatform::register);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TilemanCommands.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) return;

        TilemanEvents.serverSideJoin((ServerPlayer) event.getEntity());
    }


    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.getServer() != null) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                TilemanEvents.tickPlayer(player);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        TilemanEvents.livingEntityDead(event.getEntity(), event.getSource());
    }
}
