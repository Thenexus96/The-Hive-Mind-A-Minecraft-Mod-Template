package net.sanfonic.hivemind.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import net.sanfonic.hivemind.HivemindTemplateMod;
import net.sanfonic.hivemind.golems.CombatGolem;

@Environment(EnvType.CLIENT)
public class CombatGolemRenderer extends MobEntityRenderer<CombatGolem, BipedEntityModel<CombatGolem>> {
    public CombatGolemRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(CombatGolem entity) {
        return new Identifier(HivemindTemplateMod.MOD_ID, "textures/entity/combat_golem.png");
    }
}
