package net.sanfonic.hivemind;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.item.Item;

import net.sanfonic.hivemind.golems.CombatGolem;
import net.sanfonic.hivemind.entity.ModEntities;
import net.sanfonic.hivemind.item.ModItems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HivemindTemplateMod implements ModInitializer {
	public static final String MOD_ID = "hivemind-template-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	// Register spawn egg
	public static final Item COMBAT_GOLEM_SPAWN_EGG = Registry.register(
			Registries.ITEM,
			new Identifier(MOD_ID, "combat_golem_spawn_egg"),
			new SpawnEggItem(ModEntities.COMBAT_GOLEM, 0x8B0000, 0xFFD700, new Item.Settings())
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModEntities.register();
		ModItems.register();

		//Register attributes AFTER entities are registered
		FabricDefaultAttributeRegistry.register(ModEntities.COMBAT_GOLEM, CombatGolem.createMobAttributes());

		//Add Spawn Egg ti creative inventroy
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
			entries.add(COMBAT_GOLEM_SPAWN_EGG);
		});

		LOGGER.info("HiveMind Mod initializing!");

		// Future Logic Goes Here

	}
}