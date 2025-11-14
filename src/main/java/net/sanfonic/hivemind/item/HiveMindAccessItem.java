package net.sanfonic.hivemind.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.sanfonic.hivemind.data.player.PlayerHiveComponent;

public class HiveMindAccessItem extends Item {
    public HiveMindAccessItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient) {
            // Check if player already has access
            if (PlayerHiveComponent.hasAccess(player)) {
                player.sendMessage(
                        Text.literal("◈ ").formatted(Formatting.DARK_PURPLE)
                        .append(Text.literal("You are already connected to the Hive.").formatted(Formatting.GRAY))
                        .append(Text.literal(" ◈").formatted(Formatting.DARK_PURPLE)),
                        false
                );
                return TypedActionResult.fail(stack);
            }

            // Grant HiveMind access to player
            PlayerHiveComponent.initializeNewMember(player);

            // Play sound effect
            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.BLOCK_BEACON_ACTIVATE,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );

            // Spawn particles in a circle around player
            if (world instanceof ServerWorld serverWorld) {
                for (int i = 0; i < 20; i++) {
                    double angle = (i / 20.0) * Math.PI * 2;
                    double radius = 1.5;
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;

                    serverWorld.spawnParticles(
                            ParticleTypes.PORTAL,
                            player.getX() + offsetX,
                            player.getY() + 1.0,
                            player.getZ() + offsetZ,
                            5, // count
                            0.1, 0.1, 0.1, // spread
                            0.05 // speed
                    );
                }

                // Central burst effect
                serverWorld.spawnParticles(
                        ParticleTypes.END_ROD,
                        player.getX(),
                        player.getY() + 1.5,
                        player.getZ(),
                        10,
                        0.3, 0.3, 0.3,
                        0.1
                );
            }

            // Send formatted message
            player.sendMessage(
                    Text.literal("◈ ").formatted(Formatting.DARK_PURPLE)
                            .append(Text.literal("You feel the Hive awaken...").formatted(Formatting.LIGHT_PURPLE))
                            .append(Text.literal(" ◈").formatted(Formatting.DARK_PURPLE)),
                    false
            );

            // TODO Phase 1 Step 2: Trigger advancement when implemented
            // AdvancementTrigger.triggerHiveMindJoin(serverPlayer);

            // Consume item if not in creative mode
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
