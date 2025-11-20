package net.sanfonic.hivemind.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.sanfonic.hivemind.Hivemind;
import net.sanfonic.hivemind.block.ModBlock;
import net.sanfonic.hivemind.item.droneItems.DroneCoreItem;

public class ModItems {
    //HiveMind Access Item
    public static final Item HIVE_MIND_ACCESS = registerItem("hive_mind_access",
            new HiveMindAccessItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));

    // Basic crafting material
    public static final Item HIVE_MATERIAL = registerItem("hive_material",
            new Item(new FabricItemSettings()));

    // Drone Core - Level 0 deployable drone
    public static final Item DRONE_CORE = registerItem("drone_core",
            new DroneCoreItem(new FabricItemSettings()
                    .maxCount(16)
                    .rarity(Rarity.UNCOMMON)));

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.add(HIVE_MATERIAL);
    }

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Hivemind.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Hivemind.LOGGER.info("ModItems Registered for" + Hivemind.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ModItemGroups.HIVEMIND_GROUP_KEY).register(entries -> {
            entries.add(HIVE_MIND_ACCESS);
            entries.add(HIVE_MATERIAL);
            entries.add(DRONE_CORE);
            entries.add(ModBlock.HIVE_MATERIAL_BLOCK);
            entries.add(ModBlock.HIVE_CORE);
            entries.add(Hivemind.DRONE_SPAWN_EGG);
        });
    }
}
