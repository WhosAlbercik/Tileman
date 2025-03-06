package com.whosalbercik.tileman.client;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.whosalbercik.tileman.client.commands.TilesCommand;
import com.whosalbercik.tileman.client.renderer.BorderRenderer;
import com.whosalbercik.tileman.client.renderer.SidePanelRenderer;
import com.whosalbercik.tileman.networking.*;
import me.x150.renderer.event.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import org.lwjgl.glfw.GLFW;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


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
    @Override
    public void onInitializeClient() {
        RenderEvents.WORLD.register(BorderRenderer::renderTiles);
        RenderEvents.WORLD.register(AreaHandler::renderSelectedArea);

        RenderEvents.HUD.register(SidePanelRenderer::render);

        ClientPlayNetworking.registerGlobalReceiver(SendTilesS2C.ID, BorderRenderer::reveiveTileFromServer);
        ClientPlayNetworking.registerGlobalReceiver(ClearRenderedTilesS2C.ID, BorderRenderer::clearRenderedTiles);
        ClientPlayNetworking.registerGlobalReceiver(SendSidePanelDataS2C.ID, SidePanelRenderer::setData);
        ClientPlayNetworking.registerGlobalReceiver(SendFriendsS2C.ID, BorderRenderer::setFriends);


        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (setArea.isPressed()) {
                AreaHandler.areaSelected();
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("selectedTiles")
                            .then(literal("transferOwnership")
                                    .then(argument("player", StringArgumentType.word())
                                            .executes(TilesCommand::transfer))));
        });



    }





}
