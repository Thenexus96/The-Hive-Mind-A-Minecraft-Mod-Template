package net.sanfonic.hivemind.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import net.sanfonic.hivemind.Hivemind;
import net.sanfonic.hivemind.entity.DroneEntity;

@Environment(EnvType.CLIENT)
public class DroneRenderer extends MobEntityRenderer<DroneEntity, BipedEntityModel<DroneEntity>> {
    public DroneRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(DroneEntity entity) {
        return new Identifier("hivemind", "textures/entity/drone.png");
    }
}
