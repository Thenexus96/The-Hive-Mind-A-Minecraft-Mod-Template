package net.sanfonic.hivemind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.sanfonic.hivemind.client.ClientEventHandler;
import net.sanfonic.hivemind.client.DroneClientHandler;
import net.sanfonic.hivemind.client.renderer.DroneRenderer;
import net.sanfonic.hivemind.client.KeyBindings;
import net.sanfonic.hivemind.client.renderer.DroneRenderer;
import net.sanfonic.hivemind.control.DroneInputHandler;
import net.sanfonic.hivemind.entity.ModEntities;

public class HivemindClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register your client handler
        DroneClientHandler.init();

        // Also make sure your input handler is registered
        DroneInputHandler.init(); // If this is client-side

        // Existing entity renderer registration
        EntityRendererRegistry.register(ModEntities.DRONE, DroneRenderer::new);

        // New keybind and GUI system
        KeyBindings.registerKeyBindings();
        ClientEventHandler.registerEvents();

        System.out.println("HiveMind client initialized"); // Debug message
    }
}
