package net.sanfonic.hivemind.item;

import net.sanfonic.hivemind.HivemindTemplateMod;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item HIVE_MIND_ACCESS = registerItem("hive_mind_access",
            new HiveMindAccessItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));

    public static final Item HIVE_CORE = new Item(new Item.Settings());

    public static void register() {
        //FIXED: Use Consistent MOD_ID
        Registry.register(Registries.ITEM, new Identifier(HivemindTemplateMod.MOD_ID, "hive_core"), HIVE_CORE);
        HivemindTemplateMod.LOGGER.info("ModItems Registered for" + HivemindTemplateMod.MOD_ID);
    }
}
