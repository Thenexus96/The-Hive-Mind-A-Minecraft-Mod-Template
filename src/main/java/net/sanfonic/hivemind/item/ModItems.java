package net.sanfonic.hivemind.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.sanfonic.hivemind.Hivemind;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModItems {
    //public static final Item HIVE_MIND_ACCESS = registerItem("hive_mind_access",
            //new HiveMindAccessItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));

    public static final Item HIVE_MATERIAL = registerItem("hive_material", new Item(new FabricItemSettings()));

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.add(HIVE_MATERIAL);
    }

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Hivemind.MOD_ID, name), item);
    }


    public static void registerModItems() {
        //FIXED: Use Consistent MOD_ID
        Hivemind.LOGGER.info("ModItems Registered for" + Hivemind.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemGroup);
    }
}
