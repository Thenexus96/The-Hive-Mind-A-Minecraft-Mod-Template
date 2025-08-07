package net.sanfonic.hivemind.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.sanfonic.hivemind.network.NetworkHandler;
import net.sanfonic.hivemind.network.packets.DroneControlPacket;
import net.sanfonic.hivemind.network.packets.DroneMovementPacket;

@Environment(EnvType.CLIENT)
public class ClientDroneController implements ClientModInitializer {
    private static boolean isDroneControlled = false;
    private static Integer controlledDroneId = null;

    @Override
    public void onInitializeClient() {
        // Register client networking receivers
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.DRONE_CONTROL_PACKET,
                (client, handler, buf, responseSender) -> {
                // Read the packet data
                    DroneControlPacket packet = DroneControlPacket.read(buf);

                    // Handle on main thread
                    client.execute(() -> {
                        setDroneControlled(packet.getDroneId(), packet.isTakingControl());
                    });
                });

        //Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isDroneControlled && client.player != null) {
                handleDroneInput(client.player);
            }
        });
    }

    public static void setDroneControlled(Integer droneId, boolean controlled) {
        isDroneControlled = controlled;
        controlledDroneId = controlled ? droneId : null;

        if (controlled) {
            onDroneControlStart();
        } else
            onDroneControlEnd();
    }

    public static boolean isDroneControlled() {
        return isDroneControlled;
    }

    public static Integer getControlledDroneId() {
        return controlledDroneId;
    }

    private static void handleDroneInput(ClientPlayerEntity player) {
        // Get player input
        boolean forward = player.input.pressingForward;
        boolean backward = player.input.pressingBack;
        boolean left = player.input.pressingLeft;
        boolean right = player.input.pressingRight;
        boolean jumping = player.input.jumping;
        boolean sneaking = player.input.sneaking;

        // Convert to movement values
        float forwardValue = forward ? 1.0f : (backward ? -1.0f : 0.0f);
        float strafeValue = right ? 1.0f : (left ? -1.0f : 0.0f);
        float upValue = jumping ? 1.0f : (sneaking ? -1.0f : 0.0f);

        // Get rotation
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        // Create and send movement packet
        DroneMovementPacket packet = new DroneMovementPacket(
                forwardValue, strafeValue, upValue,
                jumping, sneaking,
                yaw, pitch
        );

        // Send to server
        sendDroneMovementPacket(packet);
    }

    public static void sendDroneMovementPacket (DroneMovementPacket packet){
        PacketByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ClientPlayNetworking.send(NetworkHandler.DRONE_MOVEMENT_PACKET, buf);
    }

    public static void sendDroneControlPacket (DroneControlPacket packet){
        PacketByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ClientPlayNetworking.send(NetworkHandler.DRONE_CONTROL_PACKET, buf);
    }

    private static void onDroneControlStart () {
        // Client-side logic for when player takes control
        // Could show HUD elements, change input handling, etc.
        System.out.println("Started controlling drone: " + controlledDroneId);
    }

    private static void onDroneControlEnd () {
        // Client-side logic for when player releases control
        // Restore normal UI state
        System.out.println("Stopped controlling drone");
    }
}