package net.sanfonic.hivemind.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.sanfonic.hivemind.entity.DroneEntity;
import net.sanfonic.hivemind.network.NetworkHandler;
import net.sanfonic.hivemind.network.packets.DroneMovementPacket;

import java.net.Inet4Address;

public class DroneClientHandler {

    private static boolean isControllingDrone = false;
    private static DroneEntity controlledDrone = null;
    private static float lastYaw = 0f;
    private static float lastPitch =0f;

    public static void init() {
        System.out.println("DroneClientHandler initializing...");

        // Handle client tick for drone control
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && isControllingDrone && controlledDrone != null) {
                handleDroneControl(client);
            }
        });

        // Handle drone control packets from server
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.DRONE_CONTROL_PACKET,
                (client, handler, buf, responseSender) -> {
            // read packet
                    boolean hasDroneId = buf.readBoolean();
                    Integer droneId = hasDroneId ? buf.readInt() : null;
                    boolean takingControl = buf.readBoolean();

                    client.execute(() -> {
                        if (takingControl && droneId != null) {
                            // Find drone and start control
                            Entity entity = client.world.getEntityById(droneId);
                            if (entity instanceof DroneEntity drone) {
                                startControllingDrone(client, drone);
                            }
                        } else {
                            // Stop controlling drone
                            stopControllingDrone(client);
                        }
                    });
                });
        System.out.println("DroneClientHandler initialized!");
    }

    private static void startControllingDrone(MinecraftClient client, DroneEntity drone) {
        isControllingDrone = true;
        controlledDrone = drone;

        // Set camera to drone
        client.setCameraEntity(drone);

        // Sync inital rotation from player to drone
        if (client.player != null) {
            drone.setYaw(client.player.getYaw());
            drone.setPitch(client.player.getPitch());

            // Set drone to player's current look direction
            drone.setYaw(lastYaw);
            drone.setPitch(lastPitch);
        }

        System.out.println("Camera switched to drone. Initial rotation: " + lastYaw + ", " + lastPitch);
    }

    private static void stopControllingDrone(MinecraftClient client) {
        System.out.println("Stopping drone control");
        isControllingDrone = false;
        controlledDrone = null;

        // Reset camera to player
        if (client.player != null) {
            client.setCameraEntity(client.player);
        }
    }

    public static void handleDroneControl(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || controlledDrone == null) return;

        // Get movement input
        float forward = 0f, strafe = 0f, up = 0f;

        if (client.options.forwardKey.isPressed()) forward += 1f;
        if (client.options.backKey.isPressed()) forward -= 1f;
        if (client.options.leftKey.isPressed()) strafe -= 1f;
        if (client.options.rightKey.isPressed()) strafe += 1f;
        if (client.options.jumpKey.isPressed()) up += 1f;
        if (client.options.sneakKey.isPressed()) up -= 1f;

        // CRITIAL: Get rotation directly from player
        // When camera is on drone, player rotation still tracks mouse movement
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        // Check if rotation changed (mouse moved)
        if (Math.abs(currentYaw - lastYaw) > 0.01f || Math.abs(currentPitch - lastPitch) > 0.01f) {
            System.out.println("Mouse moved! New rotation: " + currentYaw + ", " + currentPitch);
            lastYaw = currentYaw;
            lastPitch = currentPitch;
        }

        // Send movement packet to server
        DroneMovementPacket packet = new DroneMovementPacket(
                forward, strafe, up,
                client.options.jumpKey.isPressed(),
                client.options.sneakKey.isPressed(),
                currentYaw, currentPitch // Use player's rotation
        );

        // Send to server
        ClientPlayNetworking.send(NetworkHandler.DRONE_MOVEMENT_PACKET,
                createPacketBuf(packet));

        // Debug movement every 40 ticks
        if (player.age % 40 == 0 && (forward != 0 || strafe != 0 || up != 0)) {
            System.out.println("Movement: F=" + forward + " S=" + strafe + " U=" + up);
        }
    }

    private static net.minecraft.network.PacketByteBuf createPacketBuf(DroneMovementPacket packet) {
        net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        packet.write(buf);
        return buf;
    }

    public static boolean isControllingDrone() {
        return isControllingDrone;
    }

    public static DroneEntity getControlledDrone() {
        return controlledDrone;
    }
}
