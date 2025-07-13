package net.sanfonic.hivemind.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import net.sanfonic.hivemind.Hivemind;
import net.sanfonic.hivemind.entity.DroneEntity;

public class ModEntities {
    public static final EntityType<DroneEntity> DRONE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Hivemind.MOD_ID, "drone"),  //FIXED: Use main MOD_ID
            FabricEntityTypeBuilder.createMob()
                    .entityFactory(DroneEntity::new)
                    .spawnGroup(SpawnGroup.CREATURE)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8F))
                    .build()
    );

    public static void register() {
        Hivemind.LOGGER.info("Registering Mod Entities for" + Hivemind.MOD_ID);
        //Future Entity Registration
    }
}
