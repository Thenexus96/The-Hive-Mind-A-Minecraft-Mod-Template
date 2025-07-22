package net.sanfonic.hivemind.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.sanfonic.hivemind.Hivemind;

public class ModItemGroups {

    public static final ItemGroup HIVEMIND_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(Hivemind.MOD_ID, "hivemind"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.hivemind"))
                    .icon(() -> new ItemStack(ModItems.HIVE_CORE)).entries((displayContext, entries) -> {
                        entries.add(ModItems.HIVE_CORE);


                    }).build());

    public static void registerItemGroups() {
        Hivemind.LOGGER.info("Registering Item Groups for " + Hivemind.MOD_ID);
    }
}
