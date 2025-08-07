package net.sanfonic.hivemind.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sanfonic.hivemind.network.packets.DroneControlPacket;
import net.sanfonic.hivemind.network.packets.DroneMovementPacket;

public class NetworkHandler {

    // Define packet identifiers
    public static final Identifier DRONE_CONTROL_PACKET = new Identifier("hivemind", "drone_control");
    public static final Identifier DRONE_MOVEMENT_PACKET = new Identifier("hivemind", "drone_movement");

    public static void init() {
        // Register server packet receivers
        ServerPlayNetworking.registerGlobalReceiver(DRONE_CONTROL_PACKET,
                (server, player, handler, buf, responseSender) -> {
            // Read packet data
            DroneControlPacket packet = DroneControlPacket.read(buf);

            // Handle on main thread
            server.execute(() -> {
               packet.handle(player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(DRONE_MOVEMENT_PACKET,
                (server, player, handler, buf, responsSender) -> {
                    // Read packet data
                    DroneMovementPacket packet = DroneMovementPacket.read(buf);

                    // Handle on main thread
                    server.execute(() -> {
                        packet.handle(player);
                    });
                });
    }

    public static void sendToClient(ServerPlayerEntity player, Identifier packetId, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, packetId, buf);
    }

    // Helper methods for sending specific packets
    public static void sendDroneControlToClient(ServerPlayerEntity player, DroneControlPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        sendToClient(player, DRONE_CONTROL_PACKET, buf);
    }

    public static void sendDroneMovementToClient(ServerPlayerEntity player, DroneMovementPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        sendToClient(player, DRONE_MOVEMENT_PACKET, buf);
    }
}
