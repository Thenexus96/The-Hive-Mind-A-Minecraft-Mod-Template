package net.sanfonic.hivemind.item;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.sanfonic.hivemind.data.HiveMindData.HiveMindData;

public class HiveMindAccessItem extends Item {
    public HiveMindAccessItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient) {
            //Example: Tag player as part of the HiveMind
            HiveMindData.setHasAccess(player, true);

            //Trigger Advancement (custom advancement JSON nneeded in datapack
            MinecraftServer server = player.getServer();
            if (server != null) {
                Advancement advancement = server.getAdvancementLoader().get(new Identifier("yourmodid:hivemind_join"));
                if (advancement != null) {
                    AdvancementProgress progress = ((ServerPlayerEntity)player).getAdvancementTracker().getProgress(advancement);
                    for (String criterion : progress.getUnobtainedCriteria()) {
                        ((ServerPlayerEntity)player).getAdvancementTracker().grantCriterion(advancement, criterion);
                    }
                }
            }
            // Consume the item if not in creative mode
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            player.sendMessage(Text.literal("You feel a connection to the HiveMind..."), false);
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
