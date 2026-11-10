package dev.lclient;

import dev.lclient.gui.ClientScreen;
import dev.lclient.module.Modules;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class LClient implements ClientModInitializer {
    private boolean wasDown;
    private boolean applied;

    @Override
    public void onInitializeClient() {
        Modules.init();
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("lclient", "hud"), Hud::render);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Modules.tick();
            if (!applied && mc.options != null) { Modules.applyPerformance(); applied = true; }
            // Open GUI with Right Shift
            boolean down = GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !wasDown && mc.screen == null && mc.player != null) mc.setScreen(new ClientScreen());
            wasDown = down;
        });
    }
}
