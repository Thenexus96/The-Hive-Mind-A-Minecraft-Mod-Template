package net.sanfonic.hivemind.control;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.sanfonic.hivemind.entity.DroneEntity;

public class DroneInputHandler {

    public static void init() {
        // Register interaction events
        UseBlockCallback.EVENT.register(((player, world, hand, blockHitResult) -> {
            if (player instanceof ServerPlayerEntity serverPlayer &&
            DroneControlManager.isPlayerControllingDrone(serverPlayer)) {

                // Handle drone-specific block interactions
                DroneEntity drone = DroneControlManager.getControlledDrone(serverPlayer);
                if (drone != null) {
                    handleDroneBlockInteraction(serverPlayer, drone);
                }
                return ActionResult.CONSUME; // Prevent normal player interactions
            }
            return ActionResult.PASS;
        }));

        UseItemCallback.EVENT.register(((player, world, hand) -> {
            if (player instanceof ServerPlayerEntity serverPlayer &&
            DroneControlManager.isPlayerControllingDrone(serverPlayer)) {

                // Handle drone-specific item instructions
                DroneEntity drone = DroneControlManager.getControlledDrone(serverPlayer);
                if (drone != null) {
                    handleDroneItemInteraction(serverPlayer, drone);
                }
                return TypedActionResult.consume(player.getStackInHand(hand)); // Prevent normal player interactions
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        }));
    }

    private static void handleDroneBlockInteraction(ServerPlayerEntity player, DroneEntity drone) {
        // Handle block interactions while controlling drone
        drone.triggerPrimaryAbility();
    }

    private static void handleDroneItemInteraction(ServerPlayerEntity serverPlayer, DroneEntity drone) {
        // Handle Item interations while controlling drone
        drone.triggerSecondaryAbility();
    }
}
