package net.sanfonic.hivemind.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.sanfonic.hivemind.control.DroneControlManager;
import net.sanfonic.hivemind.control.DroneControlSession;
import net.sanfonic.hivemind.entity.DroneEntity;

public class DroneControlCommands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, RegistryAccess, Environment) -> {
            registerCommands(dispatcher);
        }));
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandManager.literal("list")
                .executes(DroneControlCommands::listDrones)
                .then(dispatcher.register(CommandManager.literal("hivemind")
                        .then(CommandManager.literal("control")
                                .then(CommandManager.argument("drone", EntityArgumentType.entity())
                                        .executes(DroneControlCommands::controlDrone)))
                        .then(CommandManager.literal("release")
                                .executes(DroneControlCommands::releaseControl))))
                .then(CommandManager.literal("controlnearest")
                        .executes(DroneControlCommands::controlNearestDrone)
                        .then(CommandManager.argument("range", IntegerArgumentType.integer(1, 100))
                                .executes(DroneControlCommands::controlNearestDroneWithRange)));
    }

    private static int controlDrone(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            Entity entity = EntityArgumentType.getEntity(context, "drone");

            if (!(entity instanceof DroneEntity drone)) {
                context.getSource().sendError(Text.literal("Target is not a drone!"));
                return 0;
            }

            if (DroneControlManager.takeControlOfDrone(player, drone)) {
                context.getSource().sendFeedback(() -> Text.literal("Taking control of drone!"), false);
                return 1;
            } else {
                context.getSource().sendError(Text.literal("Could not take control of drone!"));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Command failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int releaseControl(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

            if (DroneControlManager.isPlayerControllingDrone(player)) {
                DroneControlManager.releaseControl(player);
                context.getSource().sendFeedback(() -> Text.literal("Released drone control!"), false);
                return 1;
            } else {
                context.getSource().sendError(Text.literal("You are not controlling any drone!"));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Command failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int listDrones(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

            // Find nearby drones
            java.util.List<DroneEntity> nearbyDrones = player.getWorld()
                    .getEntitiesByClass(DroneEntity.class,
                            player.getBoundingBox().expand(50.0),
                            drone -> !DroneControlManager.isDroneControlled(drone));

            if (nearbyDrones.isEmpty()) {
                context.getSource().sendFeedback(() -> Text.literal("No available drones found nearby."), false);
            } else {
                context.getSource().sendFeedback(() -> Text.literal("Found " + nearbyDrones.size() + " available drones:"), false);

                // Create final variables for lambda capture
                for (int i = 0; i < nearbyDrones.size(); i++){
                    DroneEntity drone = nearbyDrones.get(i);
                    double distance = player.distanceTo(drone);

                    // Create effectively final variables for lambda
                    final int droneNumber = i + 1;
                    final double finalDistance = distance;
                    final double droneX = drone.getX();
                    final double droneY = drone.getY();
                    final double droneZ = drone.getZ();

                    context.getSource().sendFeedback(() -> Text.literal(
                            String.format(" %d. Drone at (%.1f, %.1f, %.1f) - Distance: %.1f blocks",
                                    droneNumber, droneX, droneY, droneZ, finalDistance)), false);
                }
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Command failed " + e.getMessage()));
            return 0;
        }
    }

    private static int controlNearestDrone(CommandContext<ServerCommandSource> context) {
        return controlNearestDroneWithRange(context, 20);
    }

    private static int controlNearestDroneWithRange(CommandContext<ServerCommandSource> context) {
        int range = IntegerArgumentType.getInteger(context, "range");
        return controlNearestDroneWithRange(context, range);
    }

    private static int controlNearestDroneWithRange(CommandContext<ServerCommandSource> context, int range) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

            // Find the nearest available drone
            DroneEntity nearestDrone = player.getWorld()
                    .getEntitiesByClass(DroneEntity.class,
                            player.getBoundingBox().expand(range),
                            drone -> !DroneControlManager.isDroneControlled(drone))
                    .stream()
                    .min((d1, d2) -> Double.compare(player.distanceTo(d1), player.distanceTo(d2)))
                    .orElse(null);

            if (nearestDrone == null) {
                context.getSource().sendError(Text.literal("No available drones found within " + range + " blocks!"));
                return 0;
            }

            if (DroneControlManager.takeControlOfDrone(player, nearestDrone)) {
                double distance = player.distanceTo(nearestDrone);
                context.getSource().sendFeedback(() -> Text.literal(
                        String.format("Taking control of nearest drone (%.1f blocks away!)", distance)), false);
                return 1;
            } else {
                context.getSource().sendError(Text.literal("Could not take control of nearest drone!"));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Command failed: " + e.getMessage()));
            return 0;
        }
    }
}