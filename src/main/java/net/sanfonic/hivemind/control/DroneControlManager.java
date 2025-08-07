package net.sanfonic.hivemind.control;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.entity.player.PlayerAbilities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.sanfonic.hivemind.entity.DroneEntity;
import net.sanfonic.hivemind.network.NetworkHandler;
import net.sanfonic.hivemind.network.packets.DroneControlPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DroneControlManager {
    private static final Map<UUID, DroneControlSession> activeSessions = new HashMap<>();
    private static final Map<UUID, PlayerState> savedPlayerStates = new HashMap<>();

    public static void init() {
        // Register server tick event
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Clean up dead drone sessions
            activeSessions.entrySet().removeIf(entry -> {
                DroneControlSession session = entry.getValue();
                if (!session.getDrone().isAlive()) {
                    ServerPlayerEntity player = session.getPlayer();
                    if (player != null && player.isAlive()) {
                        releaseControl(player);
                    }
                    return true;
                }
                return false;
            });
        });
    }

    public static boolean takeControlOfDrone(ServerPlayerEntity player, DroneEntity drone) {
        if (drone == null || !drone.isAlive()) {
            return false;
        }

        // Check if drone is already controlled
        if (isDroneControlled(drone)) {
            return false;
        }

        // Save player's current state
        savePlayerStates(player);

        // Create control session
        DroneControlSession session = new DroneControlSession(player, drone);
        activeSessions.put(player.getUuid(), session);

        // Transfer control
        transferControlToDrone(player, drone);

        return true;
    }

    public static void releaseControl(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        DroneControlSession session = activeSessions.get(playerId);

        if (session != null) {
            // Return camera to player
            player.networkHandler.sendPacket(new SetCameraEntityS2CPacket(player));

            // Restore player state
            restorePlayerState(player);

            // Resume AI control of drone
            if (session.getDrone().isAlive()) {session.getDrone().resumeAIControl();}

            // Cleanup
            activeSessions.remove(playerId);
            savedPlayerStates.remove(playerId);

            // Notify client
            DroneControlPacket controlPacket = new DroneControlPacket(null, false);
            NetworkHandler.sendDroneControlToClient(player, controlPacket);
        }
    }

    private static void transferControlToDrone(ServerPlayerEntity player, DroneEntity drone) {
        // Pause AI control
        drone.pauseAIControl();

        // Set camera to drone
        player.networkHandler.sendPacket(new SetCameraEntityS2CPacket(drone));

        // Modify player abilities from drone control
        PlayerAbilities abilities = new PlayerAbilities();
        abilities.flying = true;
        abilities.allowFlying = true;
        abilities.invulnerable = true;
        abilities.creativeMode = false;
        abilities.allowModifyWorld = false;
        abilities.setWalkSpeed(0.1f);
        abilities.setFlySpeed(drone.getFlySpeed());

        player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(abilities));

        // Notify client of control transfer
        DroneControlPacket controlPacket = new DroneControlPacket(drone.getId(), true);
        NetworkHandler.sendDroneControlToClient(player, controlPacket);
    }

    private static void savePlayerStates(ServerPlayerEntity player) {
        // Create a new PlayerAbilites and copy values manually
        PlayerAbilities originalAbilites = player.getAbilities();
        PlayerAbilities copiedAbilites = new PlayerAbilities();
        copiedAbilites.allowFlying = originalAbilites.allowFlying;
        copiedAbilites.flying = originalAbilites.flying;
        copiedAbilites.invulnerable = originalAbilites.invulnerable;
        copiedAbilites.creativeMode = originalAbilites.creativeMode;
        copiedAbilites.allowModifyWorld = originalAbilites.allowModifyWorld;
        copiedAbilites.setFlySpeed(originalAbilites.getFlySpeed());
        copiedAbilites.setWalkSpeed(originalAbilites.getWalkSpeed());

        PlayerState state = new PlayerState(
                player.getPos(),
                player.getPitch(),
                player.getYaw(),
                copiedAbilites
        );
        savedPlayerStates.put(player.getUuid(), state);
    }

    private static void restorePlayerState(ServerPlayerEntity player) {
        PlayerState state = savedPlayerStates.get(player.getUuid());
        if (state != null) {
            player.teleport(state.getPosition().x, state.getPosition().y, state.getPosition().z);
            player.setPitch(state.getxRot());

            // Manually copied abilities back
            PlayerAbilities playerAbilities = player.getAbilities();
            PlayerAbilities savedAbilites = state.getAbilities();
            playerAbilities.allowFlying = savedAbilites.allowFlying;
            playerAbilities.flying = savedAbilites.flying;
            playerAbilities.invulnerable = savedAbilites.invulnerable;
            playerAbilities.creativeMode = savedAbilites.creativeMode;
            playerAbilities.allowModifyWorld = savedAbilites.allowModifyWorld;
            playerAbilities.setFlySpeed(savedAbilites.getFlySpeed());
            playerAbilities.setWalkSpeed(savedAbilites.getWalkSpeed());

            player.sendAbilitiesUpdate();
            player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(playerAbilities));
        }
    }

    public static boolean isPlayerControllingDrone(ServerPlayerEntity player) {
        return activeSessions.containsKey(player.getUuid());
    }

    public static boolean isDroneControlled(DroneEntity drone) {
        return activeSessions.values().stream()
                .anyMatch(session -> session.getDrone().equals(drone));
    }

    public static DroneEntity getControlledDrone(ServerPlayerEntity player) {
        DroneControlSession session = activeSessions.get(player.getUuid());
        return session != null ? session.getDrone() : null;
    }
}
