package net.sanfonic.hivemind.data.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles saving and loading player HiveMind data
 */
public class PlayerDataEvents {

    public static void register() {
        System.out.println("[HiveMind] Registering PlayerDataEvents...");

        // Load data when player joins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            System.out.println("[HiveMind] === Player Join Event for: " + player.getName().getString() + " ===");
            PlayerHiveComponent.onPlayerJoin(player);

            // Debug: Check if player has access after loading
            boolean hasAccess = PlayerHiveComponent.hasAccess(player);
            System.out.println("[HiveMind] === Player " + player.getName().getString() + " has access: " + hasAccess);
        });


        // Save data when player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            System.out.println("[HiveMind] === Player Disconnect Event for: " + player.getName().getString() + " ===");
            PlayerHiveComponent.onPlayerLeave(player);

            // Debug: Check player's data before saving
            boolean hasAccess = PlayerHiveComponent.hasAccess(player);
            System.out.println("[HiveMind] Saving data for " + player.getName().getString() + " (has access: " + hasAccess + ")");

            PlayerHiveComponent.onPlayerLeave(player);
        });

        // Handle respawn - Copy data to new entity
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            System.out.println("[HiveMind] === Player Respawn Event ===");
            // The data is store by UUID, so it should carry over automatically
            // Just force a save to be safe
            if (PlayerHiveComponent.hasAccess(newPlayer)) {
                PlayerHiveComponent.setAccess(newPlayer, true);
            }
        });

        System.out.println("[HiveMind] Player data events registered!");
    }
}
