package net.sanfonic.hivemind.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.sanfonic.hivemind.Hivemind;
import net.sanfonic.hivemind.block.ModBlock;

public class ModItemGroups {

    // Create the registry key
    public static final RegistryKey<ItemGroup> HIVEMIND_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            new Identifier(Hivemind.MOD_ID, "hivemind")
    );

    public static final ItemGroup HIVEMIND_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(Hivemind.MOD_ID, "hivemind"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.hivemind"))
                    .icon(() -> new ItemStack(ModBlock.HIVE_CORE)).entries((displayContext, entries) -> {
                        entries.add(ModItems.HIVE_MATERIAL);
                        entries.add(ModBlock.HIVE_MATERIAL_BLOCK);
                        entries.add(ModBlock.HIVE_CORE);
                        entries.add(ModItems.HIVE_MIND_ACCESS);
                        entries.add(Hivemind.DRONE_SPAWN_EGG);
                    }).build());

    public static void registerItemGroups() {
        Hivemind.LOGGER.info("Registering Item Groups for " + Hivemind.MOD_ID);
    }
}
