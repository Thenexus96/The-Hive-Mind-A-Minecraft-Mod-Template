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
    public static final EntityType<CombatGolem> COMBAT_GOLEM = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(HivemindTemplateMod.MOD_ID, "combat_golem"),  //FIXED: Use main MOD_ID
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CombatGolem::new) //FIXED: Changed from MONSTER to CREATURE
            .dimensions(EntityDimensions.fixed(0.75f, 1.75f))
            .build()
    );

    public static void register() {
        HivemindTemplateMod.LOGGER.info("Registering Mod Entities for" + HivemindTemplateMod.MOD_ID);
        //Future Entity Registration
    }
}
