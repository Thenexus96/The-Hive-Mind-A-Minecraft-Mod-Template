package net.sanfonic.hivemind.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sanfonic.hivemind.control.DroneControlManager;
import net.sanfonic.hivemind.entity.DroneEntity;

public class DroneControlPacket {
    private final Integer droneId;
    private final boolean takingControl;

    public DroneControlPacket(Integer droneId, boolean takingControl) {
        this.droneId = droneId;
        this.takingControl =takingControl;
    }

    public static DroneControlPacket read(PacketByteBuf buf) {
        boolean hasDroneId = buf.readBoolean();
        Integer droneId = hasDroneId ? buf.readInt() : null;
        boolean takingControl = buf.readBoolean();

        return new DroneControlPacket(droneId, takingControl);
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(droneId != null);
        if (droneId != null) {
            buf.writeInt(droneId);
        }
        buf.writeBoolean(takingControl);
    }

    public void handle(ServerPlayerEntity player) {
        if (takingControl) {
            // Player wants to take control of a drone
            if (droneId != null) {
                // Find the drone by ID in the world
                DroneEntity drone = findDroneById(player, droneId);
                if (drone != null) {
                    DroneControlManager.takeControlOfDrone(player, drone);
                }
            }
        } else {
            // Player wants to release control
            if (DroneControlManager.isPlayerControllingDrone(player)) {
                DroneControlManager.releaseControl(player);
            }
        }
    }

    private DroneEntity findDroneById(ServerPlayerEntity player, int droneId) {
        // Search for the drone in the player's world by ID
        return player.getWorld().getEntitiesByClass(DroneEntity.class,
                player.getBoundingBox().expand(100.0),
                drone -> drone.getId() == droneId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    // Getters for accessing packet data
    public Integer getDroneId() {
        return droneId;
    }

    public boolean isTakingControl() {
        return takingControl;
    }

    public boolean takingControl() {
        return takingControl;
    }
}