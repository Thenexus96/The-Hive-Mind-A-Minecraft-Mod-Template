package net.sanfonic.hivemind.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item HIVE_CORE = new Item(new Item.Settings());

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier("hivemind", "hive_core"), HIVE_CORE);
        System.out.println("ModItems Registered!");
    }
}
