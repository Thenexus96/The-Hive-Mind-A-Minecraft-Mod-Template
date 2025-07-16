package net.sanfonic.hivemind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.sanfonic.hivemind.client.renderer.DroneRenderer;
import net.sanfonic.hivemind.entity.ModEntities;

public class HivemindClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DRONE, DroneRenderer::new);
    }
}
