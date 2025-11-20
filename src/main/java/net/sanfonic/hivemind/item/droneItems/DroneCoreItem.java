package net.sanfonic.hivemind.item.droneItems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.sanfonic.hivemind.data.player.PlayerHiveComponent;
import net.sanfonic.hivemind.entity.DroneEntity;
import net.sanfonic.hivemind.entity.ModEntities;

public class DroneCoreItem extends Item {
    public DroneCoreItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (player == null) {
            return ActionResult.FAIL;
        }

        // Check if player has HiveMind access
        if (!PlayerHiveComponent.hasAccess(player)) {
            if (!world.isClient) {
                player.sendMessage(
                        Text.literal("⚠ ").formatted(Formatting.RED)
                                .append(Text.literal("You need HiveMind access to deploy drones!").formatted(Formatting.GRAY)),
                        false
                );
            }
            return ActionResult.FAIL;
        }

        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;

            // Get the position to spawn the drone (above the clicked block)
            Direction facing = context.getSide();
            BlockPos spawnPos = pos.offset(facing);

            // Check if there's enough space
            if (!world.isAir(spawnPos) || !world.isAir(spawnPos.up())) {
                player.sendMessage(
                        Text.literal("⚠ Not enough space to deploy drone!").formatted(Formatting.RED),
                        false
                );
                return ActionResult.FAIL;
            }

            // Create and spawn the drone
            DroneEntity drone = ModEntities.DRONE.create(world);
            if (drone != null) {
                // Position the drone
                drone.refreshPositionAndAngles(
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        player.getYaw(),
                        0.0F
                );

                // Link the drone to the player
                drone.setHiveMindOwnerUuid(player.getUuid());

                // Spawn the entity
                world.spawnEntity(drone);

                // Play activation sound
                world.playSound(
                        null,
                        spawnPos,
                        SoundEvents.BLOCK_BEACON_POWER_SELECT,
                        SoundCategory.BLOCKS,
                        1.0F,
                        1.2F
                );

                // Spawn activation particles
                spawnActivationParticles(serverWorld, spawnPos);

                // Send success message with HiveCode
                String hiveCode = drone.getHiveCode();
                player.sendMessage(
                        Text.literal("✓ ").formatted(Formatting.GREEN)
                                .append(Text.literal("Drone ").formatted(Formatting.GRAY))
                                .append(Text.literal(hiveCode).formatted(Formatting.GOLD, Formatting.BOLD))
                                .append(Text.literal(" deployed!").formatted(Formatting.GRAY)),
                        false
                );

                // Consume the item
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.success(world.isClient);
    }

    /**
     * Spawn particle effects when a drone is deployed
     */
    private void spawnActivationParticles(ServerWorld world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Central burst
        world.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                20, // count
                0.3, 0.5, 0.3, // spread
                0.1 // speed
        );

        // Rising spiral
        for (int i = 0; i < 30; i++) {
            double angle = (i / 30.0) * Math.PI * 4; // Two full rotations
            double radius = 1.0;
            double height = (i / 30.0) * 2.0;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            world.spawnParticles(
                    ParticleTypes.END_ROD,
                    x + offsetX,
                    y + height,
                    z + offsetZ,
                    1, // count
                    0.0, 0.0, 0.0, // spread
                    0.02 // speed
            );
        }

        // Ground circle
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2;
            double radius = 1.5;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            world.spawnParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x + offsetX,
                    y + 0.1,
                    z + offsetZ,
                    3, // count
                    0.1, 0.1, 0.1, // spread
                    0.01 // speed
            );
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // Give it an enchanted look
    }
}