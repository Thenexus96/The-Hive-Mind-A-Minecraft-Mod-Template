package net.sanfonic.hivemind.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.sanfonic.hivemind.entity.DroneEntity;
import net.sanfonic.hivemind.network.NetworkHandler;
import net.sanfonic.hivemind.network.packets.DroneControlPacket;
import net.sanfonic.hivemind.network.packets.DroneMovementPacket;

@Environment(EnvType.CLIENT)
public class DroneClientHandler implements ClientModInitializer {

    // Control State
    private static boolean isControllingDrone = false;
    private static DroneEntity controlledDrone = null;
    private static Integer controlledDroneId = null; // Added missing variable

    // Input tracking for optimization
    private static float lastYaw = 0f;
    private static float lastPitch = 0f;
    private static float lastForward = 0f;
    private static float lastStrafe = 0f;
    private static float lastUp = 0f;
    private static boolean lastJumping = false;
    private static boolean lastSneaking = false;

    // Packet rate limiting
    private static int packetTimer = 0;
    private static final int PACKET_SEND_RATE = 2; // Send every 2 ticks instead of every tick
    private static final float INPUT_THRESHOLD = 0.01f; // Minimum change to trigger packet send

    @Override
    public void onInitializeClient() {
        System.out.println("DroneClientHandler initializing...");

        // Register client networking receivers
        registerNetworkHandlers();

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && isControllingDrone && controlledDrone != null) {
                handleDroneControl(client);
            }
        });

        System.out.println("DroneClientHandler initialized");
    }

        private static void registerNetworkHandlers() {
        // Handle drone control packets from server
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.DRONE_CONTROL_PACKET,
                (client, handler, buf, responseSender) -> {
            // read packet using proper method
                    DroneControlPacket packet = DroneControlPacket.read(buf);

                    client.execute(() -> {
                        setDroneControlled(packet.getDroneId(), packet.isTakingControl());
                    });
                });
    }

    public static void setDroneControlled(Integer droneId, boolean controlled) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (controlled && droneId != null) {
            // Find and start controlling drone
            if (client.world != null) {
                Entity entity = client.world.getEntityById(droneId);
                if (entity instanceof DroneEntity drone) {
                    startControllingDrone(client, drone);
                }
            }
        } else {
            // Stop controlling drone
            stopControllingDrone(client);
        }
    }

    private static void startControllingDrone(MinecraftClient client, DroneEntity drone) {
        System.out.println("Starting drone control for drone ID: " + drone.getId());

        // Set control state
        isControllingDrone = true;
        controlledDrone = drone;
        controlledDroneId = drone.getId();

        // Initialize rotation for tracking, sync drone rotation with player BEFORE switching camera
        if (client.player != null) {
            float playerYaw = client.player.getYaw();
            float playerPitch = client.player.getPitch();

            // Store for tracking
            lastYaw = client.player.getYaw();
            lastPitch = client.player.getPitch();

            // Set drone to player's current look direction immediately on client
            updateDroneRotationImmediate(drone, playerYaw, playerPitch);

            // Small delay to ensure rotation is set before camera switch
            client.execute(() -> {
                // Set camera to drone after ensuring rotation is synced
                client.setCameraEntity(drone);

                // Force a camera update
                if (client.gameRenderer != null) {
                    client.gameRenderer.reset();
                }
            });
        } else {
            // Fallback if no player
            client.setCameraEntity(drone);
        }

        // Reset input tracking
        resetInputTracking();

        // Show user feedback
        onDroneControlStart();
    }

    private static void stopControllingDrone(MinecraftClient client) {
        System.out.println("Stopping drone control");

        // Reset control state
        isControllingDrone = false;
        controlledDrone = null;
        controlledDroneId = null;

        // Reset camera to player
        if (client.player != null) {
            client.setCameraEntity(client.player);
        }

        // Show user feedback
        onDroneControlEnd();
    }

    public static void handleDroneControl(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || controlledDrone == null) return;

        // Get movement input using both input methods for robustness
        float forward = 0f, strafe = 0f, up = 0f;
        boolean jumping = false, sneaking = false;

        // Method 1. Direct key checking (more reliable)
        if (client.options.forwardKey.isPressed()) forward += 1f;
        if (client.options.backKey.isPressed()) forward -= 1f;
        if (client.options.leftKey.isPressed()) strafe -= 1f;
        if (client.options.rightKey.isPressed()) strafe += 1f;
        if (client.options.jumpKey.isPressed()) {
            up += 1f;
            jumping = true;
        }
        if (client.options.sneakKey.isPressed()) {
            up -= 1f;
            sneaking = true;
        }

        // CRITICAL: Get rotation directly from player
        // When camera is on drone, player rotation still tracks mouse movement
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        // Check for significant changes
        boolean rotationChanged = Math.abs(currentYaw - lastYaw) > INPUT_THRESHOLD ||
                Math.abs(currentPitch - lastPitch) > INPUT_THRESHOLD;

        boolean movementChanged = Math.abs(forward - lastForward) > INPUT_THRESHOLD ||
                Math.abs(strafe - lastStrafe) > INPUT_THRESHOLD ||
                Math.abs(up - lastUp) > INPUT_THRESHOLD ||
                jumping != lastJumping ||
                sneaking != lastSneaking;

        // Check if rotation changed (mouse moved)
        // IMMEDIATELY apply rotation to drone on client side for smooth camera
        if (rotationChanged) {
            // Apply smoothing
            float smoothYaw = net.minecraft.util.math.MathHelper.lerp(0.1f, lastYaw, currentYaw);
            float smoothPitch = net.minecraft.util.math.MathHelper.lerp(0.1f, lastPitch, currentPitch);

            // Update drone rotation on client immediately
            updateDroneRotationImmediate(controlledDrone, currentYaw, currentPitch);

            lastYaw = currentYaw;
            lastPitch = currentPitch;
        }

        // Increment packet timer
        packetTimer++;

        // Determine if we should send a packet
        boolean hasActiveInput = forward != 0f || strafe != 0f || up != 0f;
        boolean shouldSendPacket = rotationChanged ||
                movementChanged ||
                hasActiveInput ||
                (packetTimer >= PACKET_SEND_RATE);

        // Send movement packet to server
        if (shouldSendPacket) {
            DroneMovementPacket packet = new DroneMovementPacket(
                    forward, strafe, up,
                    jumping, sneaking,
                    currentYaw, currentPitch // Use player's rotation
            );

            sendDroneMovementPacket(packet);

            // Update tracking variables
            lastForward = forward;
            lastStrafe = strafe;
            lastUp = up;
            lastJumping = jumping;
            lastSneaking = sneaking;
            packetTimer = 0;

            // Debug output
            if (player.age % 60 == 0 && (hasActiveInput || rotationChanged)) {
                System.out.println("Drone Input - Movement: F=" + String.format("%.1f", forward) +
                        " S=" + String.format("%.1f", strafe) + " U=" + String.format("%.1f", up) +
                        " Rotation: Y=" + String.format("%.1f", currentYaw) +
                        " P=" + String.format("%.1f", currentPitch));
            }
        }
    }

    // Helper method to immediately update drone rotation on client side
    private static void updateDroneRotationImmediate(DroneEntity drone, float yaw, float pitch) {
     // Use the drone's method, but also ensure client-side smoothness
        drone.updateRotationImmediate(yaw,pitch);

        // Additional client-side smoothing
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.world.isClient) {
            // Force the drone's rotation to be exactly what we want
            drone.refreshPositionAndAngles(drone.getX(), drone.getY(), drone.getZ(), yaw, pitch);
        }
    }

    private static void resetInputTracking() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            lastYaw = client.player.getYaw();
            lastPitch = client.player.getPitch();
        }
        lastForward = 0f;
        lastStrafe = 0f;
        lastUp = 0f;
        lastJumping = false;
        lastSneaking = false;
        packetTimer = 0;
    }

    // Network packet sending methods
    public static void sendDroneMovementPacket(DroneMovementPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ClientPlayNetworking.send(NetworkHandler.DRONE_MOVEMENT_PACKET, buf);
    }

    public static void sendDroneControlPacket(DroneControlPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ClientPlayNetworking.send(NetworkHandler.DRONE_CONTROL_PACKET, buf);
    }

    // User feedback methods
    private static void onDroneControlStart() {
        System.out.println("Started controlling drone: " + controlledDroneId);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§aDrone control activated"), true);
        }
    }

    private static void onDroneControlEnd() {
        System.out.println("Stopped controlling drone");

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§cDrone control deactivated"), true);
        }
    }

    // Public getters for other classes to check control state
    public static boolean isControllingDrone() {
        return isControllingDrone;
    }

    public static DroneEntity getControlledDrone() {
        return controlledDrone;
    }

    public static Integer getControlledDroneId() {
        return controlledDroneId;
    }

    // Legacy compatibility method (if anything else calls this)
    public static boolean isDroneControlled() {
        return isControllingDrone;
    }

    // Static initialization method for non-ClientModIntialized usage
    public static void init() {
        // This method can be called from your main mod initializer if needed
        // The actual initialization happens in onInitializeClient()
        System.out.println("DroneClientHandler.init() called - initialization will happen on onInitializeClient()");
    }
}
