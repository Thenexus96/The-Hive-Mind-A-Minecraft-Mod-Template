package net.sanfonic.hivemind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.sanfonic.hivemind.client.renderer.CombatGolemRenderer;
import net.sanfonic.hivemind.entity.ModEntities;

public class HivemindTemplateModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.COMBAT_GOLEM, CombatGolemRenderer::new);
    }
}
