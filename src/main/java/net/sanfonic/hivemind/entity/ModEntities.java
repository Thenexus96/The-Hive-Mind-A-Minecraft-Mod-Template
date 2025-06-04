package net.sanfonic.hivemind.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sanfonic.hivemind.HivemindTemplateMod;
import net.sanfonic.hivemind.golems.CombatGolem;

public class ModEntities {
    public static final EntityType<CombatGolem> COMBAT_GOLEM = Registry.register(Registries.ENTITY_TYPE,
            new Identifier("hivemind", "combat_golem"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CombatGolem::new)
            .dimensions(EntityDimensions.fixed(0.75f, 1.75f))
            .build()
    );

    public static void register() {
        HivemindTemplateMod.LOGGER.info("Registering Mod Entities for" + HivemindTemplateMod.MOD_ID);
        //Future Entity Registration
    }
}
