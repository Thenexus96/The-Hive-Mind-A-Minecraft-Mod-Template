package net.sanfonic.hivemind.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.sanfonic.hivemind.entity.DroneEntity;

import java.util.ArrayList;
import java.util.List;

public class HiveMindCommands {
    // Add this flag to control debug output
    private static final boolean DEBUG_ENABLED = false;

    private static void debugLog(String message) {
        if (DEBUG_ENABLED) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            // Register hivemind_link command
            commandDispatcher.register(
                    CommandManager.literal("hivemind_link")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ServerPlayerEntity player = source.getPlayer();

                                // Check if player is null (command executed from console)
                                if (player == null) {
                                    source.sendFeedback(() -> Text.literal("This command can only be executed by a player!"), false);
                                    return 1;
                                }

                                System.out.println("Player executing command: " + player.getName().getString());
                                System.out.println("Player position: " + player.getPos());
                                System.out.println("Player looking at: " + player.getRotationVector());

                                // Check what the player is looking at. Also Method 1: Try the original raycast increased distance
                                HitResult hitResult = player.raycast(20.0, 1.0f, false); // Increased Distance
                                System.out.println("Hit result type: " + hitResult.getType());
                                System.out.println("Hit result pos: " + player.getRotationVector());

                                if (hitResult instanceof EntityHitResult entityHit) {
                                    Entity target = entityHit.getEntity();
                                    System.out.println("Entity found via raycast: " + target.getClass().getName());
                                    if (target instanceof DroneEntity drone) {
                                        System.out.println("Found DroneEntity via raycast, linking...");
                                        drone.setHiveMindOwnerUuid(player);
                                        source.sendFeedback(() -> Text.literal("Linked Drone to HiveMind!"), false);
                                        return 1;
                                    }
                                }

                                // Method 2: If raycast fails, try looking for nearby entities
                                System.out.println("Raycast failed, trying nearby entity search...");
                                World world = player.getWorld();
                                Vec3d playerPos = player.getPos();
                                Vec3d lookDirection = player.getRotationVector();

                                // Create a box in the direction the player is looking
                                Vec3d endPos = playerPos.add(lookDirection.multiply(10.0));
                                Box searchBox = new Box(playerPos, endPos).expand(2.0);

                                List<Entity> nearbyEntities = world.getOtherEntities(player, searchBox);
                                System.out.println("Found " + nearbyEntities.size() + "nearby entities");

                                for (Entity entity : nearbyEntities) {
                                    System.out.println("Nearby entity: " + entity.getClass().getSimpleName() + " at " + entity.getPos());
                                    if (entity instanceof DroneEntity drone) {
                                        // Check if this drone is roughly in the direction the player is looking
                                        Vec3d toEntity = entity.getPos().subtract(playerPos).normalize();
                                        double dot = lookDirection.dotProduct(toEntity);
                                        System.out.println("Dot product (looking direction): " + dot);

                                        if (dot > 0.5) { // Player is looking somewhat towards the entity
                                            System.out.println("Found DroneEntity via area search, Linking...");
                                            drone.setHiveMindOwnerUuid(player);
                                            source.sendFeedback(() -> Text.literal("Linked Drone to Hivemind! (Found via area search)"), false);
                                            return 1;
                                        }
                                    }
                                }

                                // Method 3: Last resort - find the closest drone within range
                                System.out.println("Area search failed, trying closest drone search...");
                                DroneEntity closestDrone = null;
                                double closestDistance = Double.MAX_VALUE;

                                List<Entity> allNearbyEntites = world.getOtherEntities(player, Box.of(playerPos, 20, 20, 20));
                                for (Entity entity : allNearbyEntites) {
                                    if (entity instanceof DroneEntity drone) {
                                        double distance = player.distanceTo(drone);
                                        System.out.println("Found drone at distance: " + distance);
                                        if (distance < closestDistance && distance < 10.0) {
                                            closestDistance = distance;
                                            closestDrone = drone;
                                        }
                                    }
                                }

                                if (closestDrone != null) {
                                    System.out.println("Found closest DroneEntity, linking...");
                                    closestDrone.setHiveMindOwnerUuid(player);
                                    final double finalDistance = closestDistance; // Make it effectively final
                                    source.sendFeedback(() -> Text.literal("Linked closest Drone to HiveMind! (Distance: " + String.format("%.1f", finalDistance) + ")"), false);
                                    return 1;
                                }
                                source.sendFeedback(() -> Text.literal("No Drone Found! Make sure you're looking at a drone or there's one nearby."), false);
                                return 1;
                            })
            );

            // Register hivemind_list command
            commandDispatcher.register(
                    CommandManager.literal("hivemind_list")
                            .requires(source -> source.hasPermissionLevel(0)) // All Players can use this
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ServerPlayerEntity player = source.getPlayer();

                                // Check if player is null (command executed from console)
                                if (player == null) {
                                    source.sendFeedback(() -> Text.literal("§cThis command can only be executed by a player!"), false);
                                    return 1;
                                }

                                // Get all entities in the world
                                World world = player.getWorld();
                                List<DroneEntity> connectedDrones = new ArrayList<>();

                                // Search in a large area around the player (or use world bounds)
                                Vec3d playerPos = player.getPos();

                                // Search in a large radius (1000 blocks) - adjust as needed
                                Box searchBox = Box.of(playerPos, 2000, 256, 2000); // 2000x256x2000 search area

                                List<Entity> allEntities = world.getOtherEntities(null, searchBox);

                                for (Entity entity : allEntities) {
                                    if (entity instanceof DroneEntity drone) {
                                        if (drone.hasHiveMindOwner()) {
                                            PlayerEntity owner = drone.getHiveMindOwnerPlayer();
                                            if (owner != null && owner.getUuid().equals(player.getUuid())) {
                                                connectedDrones.add(drone);
                                            }
                                        }
                                    }
                                }

                                // Display Results
                                if (connectedDrones.isEmpty()) {
                                    source.sendFeedback(() -> Text.literal("§eYou have no connected Drones."), false);
                                } else {
                                    source.sendFeedback(() -> Text.literal("§a=== Your Connected Drones ==="), false);
                                    source.sendFeedback(() -> Text.literal("§7Found " + connectedDrones.size() + " connected drone(s):"), false);

                                    for (int i = 0; i < connectedDrones.size(); i++) {
                                        DroneEntity drone = connectedDrones.get(i);
                                        Vec3d pos = drone.getPos();
                                        double distance = player.distanceTo(drone);

                                        // Format position and distance
                                        String posStr = String.format("%.1f, %.1f, %.1f", pos.x, pos.y, pos.z);
                                        String distStr = String.format("%.1f", distance);

                                        // Health info (fixed the format string)
                                        String healthStr = String.format("%.1f/%.1f", drone.getHealth(), drone.getMaxHealth());

                                        final int droneNum = i + 1;
                                        final String finalPosStr = posStr;
                                        final String finalDistStr = distStr;
                                        final String finalHealthStr = healthStr;

                                        source.sendFeedback(() -> Text.literal("§b" + droneNum + ". §7Drone at §f" + finalPosStr +
                                                " §7(§a" + finalDistStr + "m away§7) §7Health: §c" + finalHealthStr), false);
                                    }
                                }

                                return 1;
                            })
            );
        }));
    }
}